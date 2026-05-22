package com.classpulse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.classpulse.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ColorPickerBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_COLORS   = "colors";
    private static final String ARG_SELECTED = "selected";

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private OnColorSelectedListener colorListener;

    public static ColorPickerBottomSheet newInstance(int[] colors, int selectedColor) {
        ColorPickerBottomSheet sheet = new ColorPickerBottomSheet();
        Bundle args = new Bundle();
        args.putIntArray(ARG_COLORS, colors);
        args.putInt(ARG_SELECTED, selectedColor);
        sheet.setArguments(args);
        return sheet;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.colorListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_color_picker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        int[] colors   = requireArguments().getIntArray(ARG_COLORS);
        int selectedColor = requireArguments().getInt(ARG_SELECTED);

        RecyclerView rv = view.findViewById(R.id.rvColors);
        rv.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rv.setAdapter(new ColorSwatchAdapter(colors, selectedColor, color -> {
            if (colorListener != null) colorListener.onColorSelected(color);
            dismiss();
        }));
    }

    // ─── Inner Adapter ────────────────────────────────────────────────────────

    private static class ColorSwatchAdapter
            extends RecyclerView.Adapter<ColorSwatchAdapter.VH> {

        private final int[] colors;
        private int selectedColor;
        private final OnColorSelectedListener listener;

        ColorSwatchAdapter(int[] colors, int selectedColor, OnColorSelectedListener listener) {
            this.colors        = colors;
            this.selectedColor = selectedColor;
            this.listener      = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_color_swatch, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            int color = colors[position];
            holder.viewColor.getBackground().setTint(color);
            holder.ivSelected.setVisibility(color == selectedColor ? View.VISIBLE : View.GONE);
            holder.itemView.setOnClickListener(v -> {
                int prev = selectedColor;
                selectedColor = color;
                notifyItemChanged(indexOf(prev));
                notifyItemChanged(position);
                listener.onColorSelected(color);
            });
        }

        @Override
        public int getItemCount() { return colors.length; }

        private int indexOf(int color) {
            for (int i = 0; i < colors.length; i++) {
                if (colors[i] == color) return i;
            }
            return -1;
        }

        static class VH extends RecyclerView.ViewHolder {
            View      viewColor;
            android.widget.ImageView ivSelected;
            VH(@NonNull View v) {
                super(v);
                viewColor  = v.findViewById(R.id.viewColor);
                ivSelected = v.findViewById(R.id.ivSelected);
            }
        }
    }
}