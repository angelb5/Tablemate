package pe.edu.pucp.tablemate.Adapters;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.type.LatLng;

import java.util.Locale;

import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.R;

public class RestaurantCardAdapter extends FirestorePagingAdapter<Restaurant, RestaurantCardAdapter.RestaurantViewHolder>  {
    /**
     * Construct a new FirestorePagingAdapter from the given {@link FirestorePagingOptions}.
     *
     * @param options
     */

    Class nextActivity;

    public RestaurantCardAdapter(@NonNull FirestorePagingOptions options, Class activity) {
        super(options);
        nextActivity = activity;
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_restaurant_card, parent, false);
        return new RestaurantViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position, @NonNull Restaurant model) {
        holder.tvNombre.setText(model.getNombre());
        holder.tvCategoria.setText(model.getCategoria());
        if (model.getNumReviews()>0){
            holder.tvRating.setText(String.format(Locale.getDefault(),"%.1f" ,model.getRating()));
        }else{
            holder.tvRating.setText("--");
        }
        if(model.getDistance() != 0){
            holder.cvDistance.setVisibility(View.VISIBLE);
            holder.tvDistance.setText(String.format(Locale.getDefault(),"%.1f" ,model.getDistance())+" km");
        }else{
            holder.cvDistance.setVisibility(View.GONE);
        }

        holder.restaurant = model;
        Glide.with(holder.itemView.getContext()).load(model.getFotosUrl().get(0))
                .placeholder(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.ic_image_placeholder_48)).dontAnimate()
                .into(holder.ivRestaurantFoto);
    }

    public class RestaurantViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurantFoto;
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvDistance;
        CardView cvDistance;
        TextView tvRating;
        Restaurant restaurant;

        public RestaurantViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRestaurantFoto = itemView.findViewById(R.id.ivRestaurantCardFoto);
            tvNombre = itemView.findViewById(R.id.tvRestaurantCardNombre);
            tvCategoria = itemView.findViewById(R.id.tvRestaurantCardCategoria);
            cvDistance = itemView.findViewById(R.id.cvRestaurantCardDistance);
            tvDistance = itemView.findViewById(R.id.tvRestaurantCardDistance);
            tvRating = itemView.findViewById(R.id.tvRestaurantCardRating);
            itemView.setOnClickListener(view -> {
                Log.d("msg", "hola desde el usuario "+restaurant.getNombre());
            });
        }
    }

}
