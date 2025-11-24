package co.edu.unipiloto.encomienda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import co.edu.unipiloto.encomienda.db.User;
import co.edu.unipiloto.encomienda.R;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> userList;
    private OnRoleEditListener onRoleEditListener;

    public interface OnRoleEditListener {
        void onRoleEdit(User user);
    }

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public void setOnRoleEditListener(OnRoleEditListener listener) {
        this.onRoleEditListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.tvName.setText(user.name);
        holder.tvEmail.setText(user.email);
        holder.tvRole.setText(user.role);
        holder.itemView.setOnClickListener(v -> {
            if (onRoleEditListener != null) {
                onRoleEditListener.onRoleEdit(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        UserViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvEmail = itemView.findViewById(R.id.tvUserEmail);
            tvRole = itemView.findViewById(R.id.tvUserRole);
        }
    }
}
