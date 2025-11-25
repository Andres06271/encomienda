package co.edu.unipiloto.encomienda.api;

import java.util.List;
import java.util.Map;

import co.edu.unipiloto.encomienda.api.dto.CreateNotificationRequest;
import co.edu.unipiloto.encomienda.api.dto.CreateShipmentRequest;
import co.edu.unipiloto.encomienda.api.dto.RemoteNotification;
import co.edu.unipiloto.encomienda.api.dto.RemoteShipment;
import co.edu.unipiloto.encomienda.api.dto.UpdateStatusRequest;
import retrofit2.Call;
import retrofit2.http.*;

public interface EncomiendaApi {

    // Shipments
    @GET("api/shipments")
    Call<List<RemoteShipment>> getShipments(@Query("courierEmail") String courierEmail);

    @GET("api/shipments/{id}")
    Call<RemoteShipment> getShipment(@Path("id") long id);

    @POST("api/shipments")
    Call<RemoteShipment> createShipment(@Body CreateShipmentRequest request);

    @PUT("api/shipments/{id}")
    Call<RemoteShipment> updateShipment(@Path("id") long id, @Body CreateShipmentRequest request);

    @PATCH("api/shipments/{id}/status")
    Call<RemoteShipment> updateShipmentStatus(@Path("id") long id, @Body UpdateStatusRequest status);

    @DELETE("api/shipments/{id}")
    Call<Void> deleteShipment(@Path("id") long id);

    @GET("api/shipments/user/{userEmail}")
    Call<List<RemoteShipment>> getShipmentsByUser(@Path("userEmail") String userEmail);

    @GET("api/shipments/status/{status}")
    Call<List<RemoteShipment>> getShipmentsByStatus(@Path("status") String status);

    // Notifications
    @GET("api/notifications")
    Call<List<RemoteNotification>> getNotifications(@Query("userEmail") String userEmail);

    @GET("api/notifications/{id}")
    Call<RemoteNotification> getNotification(@Path("id") long id);

    @POST("api/notifications")
    Call<RemoteNotification> createNotification(@Body CreateNotificationRequest request);

    @GET("api/notifications/unread-count")
    Call<Map<String, Long>> getUnreadCount(@Query("userEmail") String userEmail);

    @GET("api/notifications/unread")
    Call<List<RemoteNotification>> getUnreadNotifications(@Query("userEmail") String userEmail);

    @PATCH("api/notifications/{id}/read")
    Call<RemoteNotification> markNotificationRead(@Path("id") long id);

    @PATCH("api/notifications/mark-all-read")
    Call<Map<String, Object>> markAllRead(@Query("userEmail") String userEmail);

    @DELETE("api/notifications/{id}")
    Call<Void> deleteNotification(@Path("id") long id);
}
