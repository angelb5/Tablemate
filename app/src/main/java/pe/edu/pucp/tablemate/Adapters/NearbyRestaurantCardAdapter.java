package pe.edu.pucp.tablemate.Adapters;

import android.content.Intent;
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
import com.denzcoskun.imageslider.interfaces.ItemClickListener;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.List;
import java.util.Locale;

import pe.edu.pucp.tablemate.Cliente.ClienteDetailsRestaurantActivity;
import pe.edu.pucp.tablemate.Entity.Restaurant;
import pe.edu.pucp.tablemate.R;

public class NearbyRestaurantCardAdapter extends
        RecyclerView.Adapter<NearbyRestaurantCardAdapter.ViewHolder> {

    private List<Restaurant> restaurantList;
    private MapboxMap map;

    public NearbyRestaurantCardAdapter(List<Restaurant> restaurantList, MapboxMap map) {
        this.restaurantList = restaurantList;
        this.map = map;
    }

    @NonNull
    @Override
    public NearbyRestaurantCardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_restaurant_map, parent, false);
        return new NearbyRestaurantCardAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NearbyRestaurantCardAdapter.ViewHolder holder, int position) {
        Restaurant restaurant = restaurantList.get(position);
        holder.tvNombre.setText(restaurant.getNombre());
        holder.tvCategoria.setText(restaurant.getCategoria());
        if (restaurant.getNumReviews()>0){
            holder.tvRating.setText(String.format(Locale.getDefault(),"%.1f" ,restaurant.getRating()));
        }else{
            holder.tvRating.setText("--");
        }
        if(restaurant.getDistance() != 0){
            holder.cvDistance.setVisibility(View.VISIBLE);
            holder.tvDistance.setText(String.format(Locale.getDefault(),"%.1f" ,restaurant.getDistance())+" km");
        }else{
            holder.cvDistance.setVisibility(View.GONE);
        }

        holder.restaurant = restaurant;
        Glide.with(holder.itemView.getContext()).load(restaurant.getFotosUrl().get(0))
                .placeholder(AppCompatResources.getDrawable(holder.itemView.getContext(), R.drawable.ic_image_placeholder_48)).dontAnimate()
                .into(holder.ivRestaurantFoto);
    }

    @Override
    public int getItemCount() {
        return restaurantList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRestaurantFoto;
        TextView tvNombre;
        TextView tvCategoria;
        TextView tvDistance;
        CardView cvDistance;
        TextView tvRating;
        Restaurant restaurant;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivRestaurantFoto = itemView.findViewById(R.id.ivRestaurantCardFoto);
            tvNombre = itemView.findViewById(R.id.tvRestaurantCardNombre);
            tvCategoria = itemView.findViewById(R.id.tvRestaurantCardCategoria);
            cvDistance = itemView.findViewById(R.id.cvRestaurantCardDistance);
            tvDistance = itemView.findViewById(R.id.tvRestaurantCardDistance);
            tvRating = itemView.findViewById(R.id.tvRestaurantCardRating);
            itemView.findViewById(R.id.btnRestaurantCard).setOnClickListener(view -> {
                Intent restaurantIntent = new Intent(itemView.getContext(), ClienteDetailsRestaurantActivity.class);
                restaurantIntent.putExtra("restaurant", restaurant);
                restaurantIntent.putExtra("lat", restaurant.getGeoPoint().getLatitude());
                restaurantIntent.putExtra("lng", restaurant.getGeoPoint().getLongitude());
                itemView.getContext().startActivity(restaurantIntent);
            });
            itemView.setOnClickListener(v -> {
                LatLng selectedLocationLatLng = new LatLng(restaurant.getGeoPoint().getLatitude(), restaurant.getGeoPoint().getLongitude());
                CameraPosition newCameraPosition = new CameraPosition.Builder()
                        .target(selectedLocationLatLng)
                        .zoom(14.5)
                        .build();
                map.easeCamera(CameraUpdateFactory.newCameraPosition(newCameraPosition));
            });
        }
    }

}
