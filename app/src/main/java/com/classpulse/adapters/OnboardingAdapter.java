package com.classpulse.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.classpulse.R;

public class OnboardingAdapter extends RecyclerView.Adapter<OnboardingAdapter.VH> {

    private final Context ctx;

    private final int[] icons = {
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher,
            R.mipmap.ic_launcher
    };

    private final String[] titles = {
            "ClassPulse",
            "Log your classes",
            "See your trends",
            "Smart feedback",
            "ClassPulse"
    };

    private final String[] subtitles = {
            "",
            "Attendance · Mood · Notes",
            "Weekly & monthly insights",
            "Personalized · Offline",
            "Ready to go!"
    };

    private final int[] layouts = {
            R.layout.page_splash,
            R.layout.page_onboard,
            R.layout.page_onboard,
            R.layout.page_onboard,
            R.layout.page_getstarted
    };

    private final int[] backgrounds = {
            0,
            0xFFD6E8FF,   // pos 1 - Log classes - light blue
            0xFFD6E8FF,   // pos 2 - See trends - light blue
            0xFFD6E8FF,   // pos 3 - Smart feedback - light blue
            0
    };

    public OnboardingAdapter(Context ctx) {
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(layouts[viewType], parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (holder.ivIcon  != null) holder.ivIcon.setImageResource(icons[position]);
        if (holder.tvTitle != null) holder.tvTitle.setText(titles[position]);
        if (holder.tvSub   != null) holder.tvSub.setText(subtitles[position]);

        if (position >= 1 && position <= 3) {
            holder.itemView.setBackgroundColor(backgrounds[position]);
            // All 3 pages get dark text on light blue background
            if (holder.tvTitle != null)
                holder.tvTitle.setTextColor(
                        android.graphics.Color.parseColor("#1C1C1E"));
            if (holder.tvSub != null)
                holder.tvSub.setTextColor(
                        android.graphics.Color.parseColor("#5A8BD0"));
        }

        if (position == 4) holder.itemView.setTag("page_4");
    }

    @Override
    public int getItemCount() { return 5; }

    @Override
    public int getItemViewType(int position) { return position; }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView  tvTitle, tvSub;

        VH(View v) {
            super(v);
            ivIcon  = v.findViewById(R.id.iv_onboard_icon);
            tvTitle = v.findViewById(R.id.tv_onboard_title);
            tvSub   = v.findViewById(R.id.tv_onboard_subtitle);
        }
    }
}