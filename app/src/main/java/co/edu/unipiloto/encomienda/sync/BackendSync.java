package co.edu.unipiloto.encomienda.sync;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import co.edu.unipiloto.encomienda.api.EncomiendaApi;
import co.edu.unipiloto.encomienda.api.RetrofitClient;
import co.edu.unipiloto.encomienda.api.dto.CreateNotificationRequest;
import co.edu.unipiloto.encomienda.api.dto.CreateShipmentRequest;
import co.edu.unipiloto.encomienda.api.dto.RemoteShipment;
import co.edu.unipiloto.encomienda.api.dto.UpdateStatusRequest;
import co.edu.unipiloto.encomienda.db.DBHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Best-effort synchronization helper between local DB (SQLite) and backend (Retrofit).
 * Fire-and-forget calls; logs errors but does not block the UI.
 */
public class BackendSync {

    private static final String TAG = "BackendSync";

    private static EncomiendaApi api() {
        return RetrofitClient.getApi();
    }

    /**
     * After assigning a shipment to a courier locally, create it on the backend and bind remoteId.
     */
    public static void pushShipmentCreateAndBind(Context context, DBHelper db, int localShipmentId) {
        try (Cursor c = db.getShipmentById(localShipmentId)) {
            if (c == null || !c.moveToFirst()) {
                Log.w(TAG, "Shipment not found: id=" + localShipmentId);
                return;
            }

            String userEmail = c.getString(c.getColumnIndexOrThrow("userEmail"));
            String courierEmail = c.getString(c.getColumnIndexOrThrow("courierEmail"));
            String address = c.getString(c.getColumnIndexOrThrow("address"));
            String type = c.getString(c.getColumnIndexOrThrow("type"));
            String status = c.getString(c.getColumnIndexOrThrow("status"));
            double latitude = c.getDouble(c.getColumnIndexOrThrow("latitude"));
            double longitude = c.getDouble(c.getColumnIndexOrThrow("longitude"));

            // Allow creating shipment without courier; can be assigned later and synced via update

            CreateShipmentRequest req = new CreateShipmentRequest(
                    address,
                    status != null ? status : "Pendiente",
                    type != null ? type : "Paquete",
                    latitude,
                    longitude,
                    userEmail,
                    courierEmail
            );

            api().createShipment(req).enqueue(new Callback<RemoteShipment>() {
                @Override
                public void onResponse(Call<RemoteShipment> call, Response<RemoteShipment> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Long remoteId = response.body().getId();
                        if (remoteId != null) {
                            boolean ok = db.setShipmentRemoteId(localShipmentId, remoteId);
                            Log.i(TAG, "Created remote shipment id=" + remoteId + ", bind local=" + localShipmentId + ", ok=" + ok);
                        }
                    } else {
                        Log.w(TAG, "createShipment failed: code=" + response.code());
                    }
                }

                @Override
                public void onFailure(Call<RemoteShipment> call, Throwable t) {
                    Log.e(TAG, "createShipment error", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "pushShipmentCreateAndBind error", e);
        }
    }

    /**
     * Push a status change to backend when we have a bound remoteId.
     */
    public static void pushShipmentStatus(DBHelper db, int localShipmentId, String newStatus) {
        Long remoteId = db.getShipmentRemoteId(localShipmentId);
        if (remoteId == null) {
            Log.i(TAG, "No remoteId for local shipment " + localShipmentId + ", skipping status sync");
            return;
        }

        api().updateShipmentStatus(remoteId, new UpdateStatusRequest(newStatus))
            .enqueue(new Callback<RemoteShipment>() {
                @Override
                public void onResponse(Call<RemoteShipment> call, Response<RemoteShipment> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Status synced for remote shipment id=" + remoteId + ": " + newStatus);
                    } else {
                        Log.w(TAG, "updateShipmentStatus failed: code=" + response.code());
                    }
                }

                @Override
                public void onFailure(Call<RemoteShipment> call, Throwable t) {
                    Log.e(TAG, "updateShipmentStatus error", t);
                }
            });
    }

    /**
     * Mirror a local notification to backend (best effort).
     */
    public static void pushNotification(String userEmail, String title, String message) {
        api().createNotification(new CreateNotificationRequest(userEmail, title, message))
            .enqueue(new Callback<co.edu.unipiloto.encomienda.api.dto.RemoteNotification>() {
                @Override
                public void onResponse(Call<co.edu.unipiloto.encomienda.api.dto.RemoteNotification> call, Response<co.edu.unipiloto.encomienda.api.dto.RemoteNotification> response) {
                    if (response.isSuccessful()) {
                        Log.i(TAG, "Notification mirrored to backend for user=" + userEmail);
                    } else {
                        Log.w(TAG, "createNotification failed: code=" + response.code());
                    }
                }

                @Override
                public void onFailure(Call<co.edu.unipiloto.encomienda.api.dto.RemoteNotification> call, Throwable t) {
                    Log.e(TAG, "createNotification error", t);
                }
            });
    }
}
