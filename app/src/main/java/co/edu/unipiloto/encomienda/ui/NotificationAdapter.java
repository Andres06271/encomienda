package co.edu.unipiloto.encomienda.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.model.LocalNotification;
import com.google.android.material.card.MaterialCardView;
import androidx.core.content.ContextCompat;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    public interface OnNotificationClickListener {
        void onNotificationClick(LocalNotification notification, int position);
    }

    private final List<LocalNotification> notifications = new ArrayList<>();
    private final OnNotificationClickListener clickListener;
    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(
        DateFormat.MEDIUM,
        DateFormat.SHORT,
        Locale.getDefault()
    );

    public NotificationAdapter(OnNotificationClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        LocalNotification notification = notifications.get(position);
        holder.bind(notification, dateFormat);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    clickListener.onNotificationClick(notification, adapterPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void updateItems(List<LocalNotification> newItems) {
        notifications.clear();
        if (newItems != null && !newItems.isEmpty()) {
            notifications.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    public LocalNotification getItem(int position) {
        return notifications.get(position);
    }

    public void refreshItem(int position) {
        notifyItemChanged(position);
    }

    public void markAllAsRead() {
        for (LocalNotification notification : notifications) {
            notification.setRead(true);
        }
        notifyDataSetChanged();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView messageView;
        private final TextView timestampView;
        private final ImageView iconView;
        private final View unreadIndicator;

        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.tvNotificationTitle);
            messageView = itemView.findViewById(R.id.tvNotificationMessage);
            timestampView = itemView.findViewById(R.id.tvNotificationTimestamp);
            iconView = itemView.findViewById(R.id.ivNotificationIcon);
            unreadIndicator = itemView.findViewById(R.id.viewUnreadIndicator);
        }

        void bind(LocalNotification notification, DateFormat dateFormat) {
            titleView.setText(notification.getTitle());
            messageView.setText(notification.getMessage());

            Date createdAt = new Date(notification.getCreatedAtMillis());
            timestampView.setText(dateFormat.format(createdAt));

            boolean isRead = notification.isRead();
            int titleStyle = isRead ? Typeface.NORMAL : Typeface.BOLD;
            titleView.setTypeface(null, titleStyle);

            int bgColorRes = isRead
                ? R.color.notification_read_background
                : R.color.notification_unread_background;
            MaterialCardView cardView = (MaterialCardView) itemView;
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), bgColorRes));

            // Stroke para destacar no leídas
            cardView.setStrokeWidth(isRead ? 0 : 4);
            cardView.setStrokeColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));

            // Mostrar indicador de no leído
            unreadIndicator.setVisibility(isRead ? View.GONE : View.VISIBLE);

            // Seleccionar ícono según título
            String title = notification.getTitle();
            if (title == null) title = "";
            if (title.contains(itemView.getContext().getString(R.string.notification_title_delivered))) {
                iconView.setImageResource(R.drawable.ic_notification_delivered);
            } else if (title.contains(itemView.getContext().getString(R.string.notification_title_route_started))) {
                iconView.setImageResource(R.drawable.ic_notification_route);
            } else if (title.contains(itemView.getContext().getString(R.string.notification_title_new_shipment))) {
                iconView.setImageResource(R.drawable.ic_notification_shipment);
            } else {
                iconView.setImageResource(android.R.drawable.ic_dialog_info);
            }
        }
    }
}
