package co.edu.unipiloto.encomienda.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.List;

import co.edu.unipiloto.encomienda.api.EncomiendaApi;
import co.edu.unipiloto.encomienda.api.RetrofitClient;
import co.edu.unipiloto.encomienda.api.dto.CreateNotificationRequest;
import co.edu.unipiloto.encomienda.api.dto.RemoteNotification;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository para gestionar Notifications consumiendo el backend REST API
 */
public class NotificationRepository {

    private static final String TAG = "NotificationRepository";
    private final EncomiendaApi api;

    public NotificationRepository() {
        this.api = RetrofitClient.getApi();
    }

    /**
     * Callback genérico para operaciones con notificaciones
     */
    public interface NotificationCallback {
        void onSuccess(RemoteNotification notification);
        void onError(String message);
    }

    /**
     * Callback para listas de notificaciones
     */
    public interface NotificationListCallback {
        void onSuccess(List<RemoteNotification> notifications);
        void onError(String message);
    }

    /**
     * Callback para conteo de notificaciones
     */
    public interface UnreadCountCallback {
        void onSuccess(int count);
        void onError(String message);
    }

    /**
     * Callback para operaciones simples (sin datos de retorno)
     */
    public interface SimpleCallback {
        void onSuccess();
        void onError(String message);
    }

    /**
     * Obtener todas las notificaciones
     */
    public void getAllNotifications(NotificationListCallback callback) {
        api.getNotifications(null).enqueue(new Callback<List<RemoteNotification>>() {
            @Override
            public void onResponse(@NonNull Call<List<RemoteNotification>> call, @NonNull Response<List<RemoteNotification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener notificaciones: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RemoteNotification>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener notificaciones", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener notificaciones por usuario
     */
    public void getNotificationsByUser(String userEmail, NotificationListCallback callback) {
        api.getNotifications(userEmail).enqueue(new Callback<List<RemoteNotification>>() {
            @Override
            public void onResponse(@NonNull Call<List<RemoteNotification>> call, @NonNull Response<List<RemoteNotification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener notificaciones del usuario: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RemoteNotification>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener notificaciones del usuario", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener una notificación específica por ID
     */
    public void getNotificationById(long id, NotificationCallback callback) {
        api.getNotification(id).enqueue(new Callback<RemoteNotification>() {
            @Override
            public void onResponse(@NonNull Call<RemoteNotification> call, @NonNull Response<RemoteNotification> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Notificación no encontrada: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RemoteNotification> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener notificación", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Crear una nueva notificación
     */
    public void createNotification(String userEmail, String title, String body, NotificationCallback callback) {
        CreateNotificationRequest request = new CreateNotificationRequest(userEmail, title, body);

        api.createNotification(request).enqueue(new Callback<RemoteNotification>() {
            @Override
            public void onResponse(@NonNull Call<RemoteNotification> call, @NonNull Response<RemoteNotification> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al crear notificación: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RemoteNotification> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al crear notificación", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener el conteo de notificaciones no leídas
     */
    public void getUnreadCount(String userEmail, UnreadCountCallback callback) {
        api.getUnreadCount(userEmail).enqueue(new Callback<java.util.Map<String, Long>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.Map<String, Long>> call, @NonNull Response<java.util.Map<String, Long>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        java.util.Map<String, Long> map = response.body();
                        Long countObj = map.get("count");
                        int count = (countObj != null) ? countObj.intValue() : 0;
                        callback.onSuccess(count);
                    } catch (Exception e) {
                        Log.e(TAG, "Error al parsear conteo de notificaciones", e);
                        callback.onError("Error al procesar respuesta: " + e.getMessage());
                    }
                } else {
                    callback.onError("Error al obtener conteo de notificaciones: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<java.util.Map<String, Long>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener conteo de notificaciones", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Obtener notificaciones no leídas
     */
    public void getUnreadNotifications(String userEmail, NotificationListCallback callback) {
        api.getUnreadNotifications(userEmail).enqueue(new Callback<List<RemoteNotification>>() {
            @Override
            public void onResponse(@NonNull Call<List<RemoteNotification>> call, @NonNull Response<List<RemoteNotification>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener notificaciones no leídas: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RemoteNotification>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al obtener notificaciones no leídas", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Marcar una notificación como leída
     */
    public void markAsRead(long id, NotificationCallback callback) {
        api.markNotificationRead(id).enqueue(new Callback<RemoteNotification>() {
            @Override
            public void onResponse(@NonNull Call<RemoteNotification> call, @NonNull Response<RemoteNotification> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al marcar como leída: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<RemoteNotification> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al marcar como leída", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Marcar todas las notificaciones como leídas
     */
    public void markAllAsRead(String userEmail, SimpleCallback callback) {
        api.markAllRead(userEmail).enqueue(new Callback<java.util.Map<String, Object>>() {
            @Override
            public void onResponse(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Response<java.util.Map<String, Object>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al marcar todas como leídas: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<java.util.Map<String, Object>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al marcar todas como leídas", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    /**
     * Eliminar una notificación
     */
    public void deleteNotification(long id, SimpleCallback callback) {
        api.deleteNotification(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onError("Error al eliminar notificación: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Error de red al eliminar notificación", t);
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }
}
