package pe.edu.pucp.tablemate.Modals;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import pe.edu.pucp.tablemate.Admin.AdminListRestaurantActivity;
import pe.edu.pucp.tablemate.R;

public class ModalBottomSheetFilter extends BottomSheetDialogFragment {
    public String TAG = "ModalBottomSheet";
    public String categoryFilter = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.modal_bottom_sheet_filter, container, false);
        ChipGroup chipGroup = view.findViewById(R.id.cgModalBottomSheet);
        chipGroup.setOnCheckedStateChangeListener(new ChipGroup.OnCheckedStateChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull ChipGroup group, @NonNull List<Integer> checkedIds) {
                for (int i = 0; i<chipGroup.getChildCount(); i++){
                    Chip c = (Chip) group.getChildAt(i);
                    if (c.isChecked()){
                        c.setTextAppearanceResource(R.style.chipTextSelected);
                        c.setTextColor(view.getContext().getColor(R.color.orange_main));
                        c.setChipStrokeColorResource(R.color.orange_main);
                        c.setChipStrokeWidthResource(R.dimen.dp1);
                        categoryFilter = c.getText().toString().equals("Todas las categorías")?"":c.getText().toString();
                    }else{
                        c.setTextAppearanceResource(R.style.chipTextUnselected);
                        c.setTextColor(view.getContext().getColor(R.color.font_dark));
                        c.setChipStrokeColorResource(R.color.font_light);
                        c.setChipStrokeWidthResource(R.dimen.dp05);
                    }
                }
            }
        });
        view.findViewById(R.id.ibModalBottomSheetClose).setOnClickListener(view1 -> {
            dismiss();
        });
        view.findViewById(R.id.btnModalBottomSheetApply).setOnClickListener(view1 -> {
            if (getActivity() instanceof AdminListRestaurantActivity){
                ((AdminListRestaurantActivity) getActivity()).setCategoryFilter(categoryFilter);
            }
            dismiss();
        });
        return view;
    }
}
