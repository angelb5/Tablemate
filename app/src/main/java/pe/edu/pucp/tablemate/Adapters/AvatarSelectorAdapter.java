package pe.edu.pucp.tablemate.Adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import pe.edu.pucp.tablemate.R;

public class AvatarSelectorAdapter extends RecyclerView.Adapter<AvatarSelectorAdapter.ViewHolder>
        implements View.OnClickListener {
    private Context context;
    private int selectedItem = 0;
    private List<String> images;
    private View.OnClickListener listener;

    public AvatarSelectorAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_avatar_selector, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (selectedItem == position) {
            holder.flOverlay.setForeground(null);
        }else{
            holder.flOverlay.setForeground(AppCompatResources.getDrawable(context,R.drawable.shape_semiblackcircle));
        }
        Glide.with(context).load(images.get(position)).into(holder.ivImage);
    }


    @Override
    public int getItemCount() {
        return images.size();
    }

    @Override
    public void onClick(View view) {
        if (listener !=null){
            listener.onClick(view);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        FrameLayout flOverlay;
        ImageView ivImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            flOverlay =itemView.findViewById(R.id.flAvatarSelector);
            ivImage = itemView.findViewById(R.id.ivAvatarSelector);
        }
    }

    public int getSelectedItem(){
        return selectedItem;
    }

    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }
}