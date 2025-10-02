package co.edu.unipiloto.encomienda.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.unipiloto.encomienda.R;
import co.edu.unipiloto.encomienda.model.Shipment;

public class ShipmentAdapter extends RecyclerView.Adapter<ShipmentAdapter.ShipmentViewHolder> {

    private List<Shipment> shipmentList;
    private OnItemClickListener listener;

    public ShipmentAdapter(List<Shipment> shipmentList) {
        this.shipmentList = shipmentList;
    }

    public interface OnItemClickListener {
        void onItemClick(Shipment shipment);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shipment, parent, false);
        return new ShipmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShipmentViewHolder holder, int position) {
        Shipment shipment = shipmentList.get(position);
        holder.txtDireccion.setText("Dirección: " + shipment.getDireccion());
        holder.txtFecha.setText("Fecha: " + shipment.getFecha());
        holder.txtHora.setText("Hora: " + shipment.getHora());
        holder.txtTipo.setText("Tipo: " + shipment.getTipo());
        holder.txtStatus.setText("Estado: " + shipment.getStatus()); // 🔹 Nuevo

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(shipmentList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return shipmentList.size();
    }

    public static class ShipmentViewHolder extends RecyclerView.ViewHolder {

        TextView txtDireccion, txtFecha, txtHora, txtTipo, txtStatus;

        public ShipmentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDireccion = itemView.findViewById(R.id.txtDireccion);
            txtFecha = itemView.findViewById(R.id.txtFecha);
            txtHora = itemView.findViewById(R.id.txtHora);
            txtTipo = itemView.findViewById(R.id.txtTipo);
            txtStatus = itemView.findViewById(R.id.txtEstado); // 🔹 Nuevo
        }
    }
}
