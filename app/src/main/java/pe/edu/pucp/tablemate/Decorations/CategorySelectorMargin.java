package pe.edu.pucp.tablemate.Decorations;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class CategorySelectorMargin extends RecyclerView.ItemDecoration {
    private final int columns;
    private final int margin;
    private final int marginHorizontal;

    public CategorySelectorMargin(int columns, int margin, int marginHorizontal) {
        this.columns = columns;
        this.margin = margin;
        this.marginHorizontal = marginHorizontal;
    }

    /**
     * Set different margins for the items inside the recyclerView: no top margin for the first row
     * and no left margin for the first column.
     */
    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {

        int position = parent.getChildLayoutPosition(view);
        if(position+1==columns){
            outRect.right = marginHorizontal;
        }else{
            outRect.right = margin;
        }
        if(position%columns==0 ){
            outRect.left = marginHorizontal;
        }
    }
}
