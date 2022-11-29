package pe.edu.pucp.tablemate.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import pe.edu.pucp.tablemate.Entity.Review;
import pe.edu.pucp.tablemate.R;


public class ReviewAdapter extends FirestorePagingAdapter<Review, ReviewAdapter.ViewHolder> {

    DateFormat df = new SimpleDateFormat("EEE dd MMM yyy", Locale.getDefault());
    private final Context context;

    public ReviewAdapter(@NonNull FirestorePagingOptions<Review> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Review review) {
        Glide.with(context).load(review.getUser().getAvatarUrl()).into(holder.ivPfp);
        holder.tvNombre.setText(review.getUser().getNombre());
        holder.tvRating.setText(String.valueOf(review.getRating()));
        holder.tvContent.setText(review.getContent());
        if (!review.getFotoUrl().isEmpty()) {
            holder.ivFoto.setVisibility(View.VISIBLE);
            Glide.with(context).load(review.getFotoUrl()).into(holder.ivFoto);
        } else {
            holder.ivFoto.setVisibility(View.GONE);
        }
        String fecha = df.format(review.getTimestamp().toDate());
        holder.tvFecha.setText(fecha);
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView ivPfp;
        TextView tvNombre;
        TextView tvRating;
        TextView tvContent;
        TextView tvFecha;
        ShapeableImageView ivFoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPfp = itemView.findViewById(R.id.ivReviewPfp);
            tvNombre = itemView.findViewById(R.id.tvReviewNombre);
            tvRating = itemView.findViewById(R.id.tvReviewRating);
            tvContent = itemView.findViewById(R.id.tvReviewContent);
            tvFecha = itemView.findViewById(R.id.tvReviewFecha);
            ivFoto = itemView.findViewById(R.id.ivReviewFoto);
        }
    }
}
