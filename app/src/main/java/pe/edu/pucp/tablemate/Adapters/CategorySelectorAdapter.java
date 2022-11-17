package pe.edu.pucp.tablemate.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pe.edu.pucp.tablemate.R;


public class CategorySelectorAdapter extends RecyclerView.Adapter<CategorySelectorAdapter.ViewHolder>
implements View.OnClickListener {
    private Context context;
    private List<Integer> images;
    private List<Integer> texts;
    private View.OnClickListener listener;

    public CategorySelectorAdapter(Context context, List<Integer> images, List<Integer> texts) {
        this.context = context;
        this.images = images;
        this.texts = texts;
    }

    public void setOnItemClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_selector, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.ivImage.setImageResource(images.get(position));
        holder.tvDesc.setText(texts.get(position));
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

        ImageView ivImage;
        TextView tvDesc;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivCategorySelector);
            tvDesc = itemView.findViewById(R.id.tvCategorySelector);
        }
    }
}
