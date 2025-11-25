package co.edu.unipiloto.encomienda.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.encomienda.api.EncomiendaApi;
import co.edu.unipiloto.encomienda.api.RetrofitClient;
import co.edu.unipiloto.encomienda.api.dto.CreateShipmentRequest;
import co.edu.unipiloto.encomienda.api.dto.RemoteShipment;
import co.edu.unipiloto.encomienda.api.dto.UpdateStatusRequest;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository para gestionar Shipments consumiendo el backend REST API
 */
public class ShipmentRepository {

    private static final String TAG = "ShipmentRepository";
    private final EncomiendaApi api;

    public ShipmentRepository() {
        this.api = RetrofitClient.getApi();
    }

    /**
     * Callback genérico para operaciones con shipments
     */
    public interface ShipmentCallback {
        void onSuccess(RemoteShipment shipment);
        void onError(String message);
    }

    /**
     * Callback para listas de shipments
     */
    public interface ShipmentListCallback {
        void onSuccess(List<RemoteShipment> shipments);
        void onError(String message);
    }

    /**
     * Callback para operaciones de eliminación
     */
    public interface DeleteCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Obtener todos los shipments
     */
    public void getAllShipments(ShipmentListCallback callback) {
        api.getShipments(null).enqueue(new Callback<List<RemoteShipment>>() {
            @Override
            public void onResponse(@NonNull Call<List<RemoteShipment>> call, @NonNull Response<List<RemoteShipment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener shipments: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RemoteShipment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener shipments", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener shipments por courier
     */
    public void getShipmentsByCourier(String courierEmail, ShipmentListCallback callback) {
        api.getShipments(courierEmail).enqueue(new Callback<List<RemoteShipment>>() {
            @Override
            public void onResponse(@NonNull Call<List<RemoteShipment>> call, @NonNull Response<List<RemoteShipment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener shipments del courier: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RemoteShipment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener shipments del courier", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener shipments por usuario
     */
    public void getShipmentsByUser(String userEmail, ShipmentListCallback callback) {
        api.getShipmentsByUser(userEmail).enqueue(new Callback<List<RemoteShipment>>() {
            @Override
            public void onResponse(@NonNull Call<List<RemoteShipment>> call, @NonNull Response<List<RemoteShipment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener shipments del usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RemoteShipment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener shipments del usuario", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener shipments por estado
     */
    public void getShipmentsByStatus(String status, ShipmentListCallback callback) {
        api.getShipmentsByStatus(status).enqueue(new Callback<List<RemoteShipment>>() {
            @Override
            public void onResponse(@NonNull Call<List<RemoteShipment>> call, @NonNull Response<List<RemoteShipment>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener shipments por estado: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RemoteShipment>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener shipments por estado", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener un shipment específico por ID
     */
    public void getShipmentById(long id, ShipmentCallback callback) {
        api.getShipment(id).enqueue(new Callback<RemoteShipment>() {
            @Override
            public void onResponse(@NonNull Call<RemoteShipment> call, @NonNull Response<RemoteShipment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Shipment no encontrado: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RemoteShipment> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener shipment", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Crear un nuevo shipment
     */
    public void createShipment(String address, String status, String type, 
                               double latitude, double longitude,
                               String userEmail, String courierEmail,
                               ShipmentCallback callback) {
        CreateShipmentRequest request = new CreateShipmentRequest(
                address, status, type, latitude, longitude, userEmail, courierEmail
        );

        api.createShipment(request).enqueue(new Callback<RemoteShipment>() {
            @Override
            public void onResponse(@NonNull Call<RemoteShipment> call, @NonNull Response<RemoteShipment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al crear shipment: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RemoteShipment> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al crear shipment", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Actualizar completamente un shipment
     */
    public void updateShipment(long id, String address, String status, String type,
                               double latitude, double longitude,
                               String userEmail, String courierEmail,
                               ShipmentCallback callback) {
        CreateShipmentRequest request = new CreateShipmentRequest(
                address, status, type, latitude, longitude, userEmail, courierEmail
        );

        api.updateShipment(id, request).enqueue(new Callback<RemoteShipment>() {
            @Override
            public void onResponse(@NonNull Call<RemoteShipment> call, @NonNull Response<RemoteShipment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al actualizar shipment: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RemoteShipment> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al actualizar shipment", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Actualizar solo el estado de un shipment
     */
    public void updateShipmentStatus(long id, String newStatus, ShipmentCallback callback) {
        UpdateStatusRequest request = new UpdateStatusRequest(newStatus);

        api.updateShipmentStatus(id, request).enqueue(new Callback<RemoteShipment>() {
            @Override
            public void onResponse(@NonNull Call<RemoteShipment> call, @NonNull Response<RemoteShipment> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al actualizar estado: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RemoteShipment> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al actualizar estado", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Eliminar un shipment
     */
    public void deleteShipment(long id, DeleteCallback callback) {
        api.deleteShipment(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al eliminar shipment: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al eliminar shipment", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}
