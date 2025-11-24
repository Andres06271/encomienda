package co.edu.unipiloto.encomienda.ui;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.db.DBHelper;
import co.edu.unipiloto.encomienda.model.LocalNotification;

public class NotificationCenterActivity extends AppCompatActivity {

    private DBHelper dbHelper;
    private NotificationAdapter adapter;
    private String userEmail;

    private TextView unreadCountView;
    private TextView emptyStateView;
    private MaterialButton markAllReadButton;
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

        dbHelper = new DBHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbarNotification);
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    toolbar.setNavigationIconTint(ContextCompat.getColor(this, android.R.color.black));
        toolbar.setNavigationOnClickListener(v -> finish());

        unreadCountView = findViewById(R.id.tvUnreadCount);
        emptyStateView = findViewById(R.id.tvEmptyState);
    markAllReadButton = findViewById(R.id.btnMarkAllRead);

        RecyclerView recyclerView = findViewById(R.id.recyclerNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new NotificationAdapter((notification, position) -> {
            if (!notification.isRead()) {
                dbHelper.markNotificationAsRead(notification.getId());
                notification.setRead(true);
                adapter.refreshItem(position);
                decrementUnreadCount();
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
        List<LocalNotification> notifications = new ArrayList<>();
        unreadCount = 0;

        try (Cursor cursor = dbHelper.getNotificationsForUser(userEmail)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String message = cursor.getString(cursor.getColumnIndexOrThrow("message"));
                    long createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("createdAt"));
                    boolean isRead = cursor.getInt(cursor.getColumnIndexOrThrow("isRead")) == 1;

                    if (!isRead) {
                        unreadCount++;
                    }

                    notifications.add(new LocalNotification(id, title, message, createdAt, isRead));
                } while (cursor.moveToNext());
            }
        }

        adapter.updateItems(notifications);
        updateUnreadCountLabel();
        updateEmptyState(notifications.isEmpty());
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

        dbHelper.markAllNotificationsAsRead(userEmail);
        unreadCount = 0;
        adapter.markAllAsRead();
        updateUnreadCountLabel();
        Toast.makeText(this, R.string.notification_mark_all_done, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
