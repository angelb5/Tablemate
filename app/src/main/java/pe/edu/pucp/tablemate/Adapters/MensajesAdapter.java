package pe.edu.pucp.tablemate.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import pe.edu.pucp.tablemate.Entity.Mensaje;
import pe.edu.pucp.tablemate.R;

public class MensajesAdapter extends RecyclerView.Adapter<MensajesAdapter.ViewHolder> {
    List<Mensaje> listaMensajes;
    private final String uid;
    DateFormat df = new SimpleDateFormat("dd MMM HH:mm", Locale.getDefault());

    public MensajesAdapter(String uid, List<Mensaje> listaMensajes) {
        this.listaMensajes = listaMensajes;
        this.uid = uid;
        setHasStableIds(true);
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView tvTimestamp;
        TextView tvMensaje;

        public ViewHolder(View view){
            super(view);
            tvMensaje = (TextView) view.findViewById(R.id.tvMensaje);
            tvTimestamp = (TextView) view.findViewById(R.id.tvTimestamp);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_orange, parent,false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bubble_white, parent,false);
        }
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mensaje mensaje = listaMensajes.get(position);
        holder.tvMensaje.setText(mensaje.getMensaje());
        holder.tvTimestamp.setText(df.format(mensaje.getTimestamp().toDate()));
    }

    @Override
    public int getItemCount() {
        return listaMensajes.size();
    }
    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (listaMensajes.get(position).getUid().equals(uid)){
            return 0;
        } else {
            return 1;
        }
    }
}
