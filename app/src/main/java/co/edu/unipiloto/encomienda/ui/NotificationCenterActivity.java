package co.edu.unipiloto.encomienda.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.api.dto.RemoteNotification;
import co.edu.unipiloto.encomienda.model.LocalNotification;
import co.edu.unipiloto.encomienda.repository.NotificationRepository;

public class NotificationCenterActivity extends AppCompatActivity {

    private NotificationRepository notificationRepository;
    private NotificationAdapter adapter;
    private String userEmail;

    private TextView unreadCountView;
    private TextView emptyStateView;
    private MaterialButton markAllReadButton;
    private ProgressBar progressBar;
    private int unreadCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_center);

        userEmail = getIntent().getStringExtra("userEmail");
        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, R.string.notification_center_missing_user, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        notificationRepository = new NotificationRepository();

        MaterialToolbar toolbar = findViewById(R.id.toolbarNotification);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    toolbar.setNavigationIconTint(ContextCompat.getColor(this, android.R.color.black));
        toolbar.setNavigationOnClickListener(v -> finish());

        unreadCountView = findViewById(R.id.tvUnreadCount);
        emptyStateView = findViewById(R.id.tvEmptyState);
        markAllReadButton = findViewById(R.id.btnMarkAllRead);
        progressBar = findViewById(R.id.progressBarNotifications);

        RecyclerView recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter((notification, position) -> {
            if (!notification.isRead()) {
                // Marcar como leída en el backend
                notificationRepository.markAsRead(notification.getId(), new NotificationRepository.NotificationCallback() {
                    @Override
                    public void onSuccess(RemoteNotification updatedNotification) {
                        notification.setRead(true);
                        adapter.refreshItem(position);
                        decrementUnreadCount();
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(NotificationCenterActivity.this, 
                                "Error al marcar como leída", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);

    markAllReadButton.setOnClickListener(v -> markAllAsRead());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        unreadCount = 0;

        // Consumir API del backend
        notificationRepository.getNotificationsByUser(userEmail, new NotificationRepository.NotificationListCallback() {
            @Override
            public void onSuccess(List<RemoteNotification> remoteNotifications) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                List<LocalNotification> notifications = new ArrayList<>();
                
                for (RemoteNotification remote : remoteNotifications) {
                    boolean isRead = remote.isReadFlag();
                    long createdAt;
                    if (remote.getCreatedAt() != null && !remote.getCreatedAt().isEmpty()) {
                        try {
                            createdAt = Instant.parse(remote.getCreatedAt()).toEpochMilli();
                        } catch (Exception e) {
                            createdAt = System.currentTimeMillis();
                        }
                    } else {
                        createdAt = System.currentTimeMillis();
                    }
                    
                    if (!isRead) {
                        unreadCount++;
                    }
                    
                    notifications.add(new LocalNotification(
                            remote.getId(), 
                            remote.getTitle(), 
                            remote.getBody(), 
                            createdAt,
                            isRead
                    ));
                }

                adapter.updateItems(notifications);
                updateUnreadCountLabel();
                updateEmptyState(notifications.isEmpty());
            }

            @Override
            public void onError(String message) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(NotificationCenterActivity.this, 
                        "Error al cargar notificaciones: " + message, Toast.LENGTH_LONG).show();
                updateEmptyState(true);
            }
        });
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void updateUnreadCountLabel() {
        unreadCountView.setText(getString(R.string.notification_unread_count, unreadCount));
        markAllReadButton.setEnabled(unreadCount > 0);
    }

    private void decrementUnreadCount() {
        if (unreadCount > 0) {
            unreadCount--;
            updateUnreadCountLabel();
        }
    }

    private void markAllAsRead() {
        if (unreadCount == 0) {
            Toast.makeText(this, R.string.notification_none_to_mark_read, Toast.LENGTH_SHORT).show();
            return;
        }

        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        notificationRepository.markAllAsRead(userEmail, new NotificationRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                unreadCount = 0;
                adapter.markAllAsRead();
                updateUnreadCountLabel();
                Toast.makeText(NotificationCenterActivity.this, 
                        R.string.notification_mark_all_done, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(NotificationCenterActivity.this, 
                        "Error al marcar todas como leídas: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
