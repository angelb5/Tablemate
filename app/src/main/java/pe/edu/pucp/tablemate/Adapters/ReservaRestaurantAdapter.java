package pe.edu.pucp.tablemate.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

import pe.edu.pucp.tablemate.Cliente.ClienteChatActivity;
import pe.edu.pucp.tablemate.Entity.Reserva;
import pe.edu.pucp.tablemate.R;
import pe.edu.pucp.tablemate.Restaurant.RestaurantChatActivity;


public class ReservaRestaurantAdapter extends FirestorePagingAdapter<Reserva, ReservaRestaurantAdapter.ViewHolder> {

    DateFormat df = new SimpleDateFormat("EEE dd MMM yyy", Locale.getDefault());
    private final Context context;

    public ReservaRestaurantAdapter(@NonNull FirestorePagingOptions<Reserva> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_reserva_restaurant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Reserva reserva) {
        Glide.with(context).load(reserva.getCliente().getAvatarUrl()).into(holder.ivFoto);
        holder.reserva = reserva;
        holder.tvNombre.setText(reserva.getCliente().getNombre());
        holder.tvNumPersonas.setText(String.valueOf(reserva.getNumPersonas()));
        holder.tvFechaReserva.setText(reserva.getFecha()+" "+reserva.getHora().toUpperCase(Locale.ROOT));
        holder.tvEstado.setText(reserva.getEstado());
        switch (reserva.getEstado()) {
            case "Pendiente":
                holder.tvEstado.setTextColor(holder.itemView.getContext().getColor(R.color.yellow));
                break;
            case "Aceptada":
                holder.tvEstado.setTextColor(holder.itemView.getContext().getColor(R.color.green));
                break;
            case "Cancelada":
                holder.tvEstado.setTextColor(holder.itemView.getContext().getColor(R.color.red));
                break;
        }
        String fecha = df.format(reserva.getSendTime().toDate());
        holder.tvFechaEnvio.setText("Enviado "+fecha);
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvNombre;
        TextView tvNumPersonas;
        TextView tvFechaReserva;
        TextView tvFechaEnvio;
        TextView tvEstado;
        ShapeableImageView ivFoto;
        Reserva reserva;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvNumPersonas = itemView.findViewById(R.id.tvNumPersonas);
            tvFechaReserva = itemView.findViewById(R.id.tvFechaReserva);
            tvFechaEnvio = itemView.findViewById(R.id.tvFechaEnvio);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            ivFoto = itemView.findViewById(R.id.ivFoto);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(itemView.getContext(), RestaurantChatActivity.class);
                    intent.putExtra("reserva", reserva);
                    intent.putExtra("tNano", reserva.getSendTime().getNanoseconds());
                    intent.putExtra("rNano", reserva.getReservaTime().getNanoseconds());
                    intent.putExtra("tSec", reserva.getSendTime().getSeconds());
                    intent.putExtra("rSec", reserva.getReservaTime().getSeconds());
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
