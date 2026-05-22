package com.classpulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.classpulse.R;
import com.classpulse.adapters.OnboardingAdapter;
import com.classpulse.utils.PrefsManager;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private View[] dots;
    private TextView tvSwipe;
    private PrefsManager prefs; // ← field declared here at top of class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.parseColor("#5A8BD0"));
        setContentView(R.layout.activity_splash);

        prefs = new PrefsManager(this); // ← assigned here, no "PrefsManager" in front

        viewPager = findViewById(R.id.viewPager);
        tvSwipe   = findViewById(R.id.tvSwipe);

        dots = new View[]{
                findViewById(R.id.dot1),
                findViewById(R.id.dot2),
                findViewById(R.id.dot3),
                findViewById(R.id.dot4),
                findViewById(R.id.dot5)
        };

        OnboardingAdapter adapter = new OnboardingAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(5);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                boolean isLast = position == 4;
                boolean isFirstPage = position == 0;

                findViewById(R.id.dotsContainer).setVisibility(
                        isLast ? View.GONE : View.VISIBLE);
                tvSwipe.setVisibility(isLast ? View.GONE : View.VISIBLE);

                // White dots on dark blue (page 0), blue dots on light blue (pages 1-3)
                int dotColor = isFirstPage ?
                        android.graphics.Color.WHITE :
                        android.graphics.Color.parseColor("#5A8BD0");
                int swipeColor = isFirstPage ?
                        android.graphics.Color.parseColor("#AACFFF") :
                        android.graphics.Color.parseColor("#5A8BD0");

                for (View dot : dots) {
                    android.graphics.drawable.GradientDrawable bg =
                            new android.graphics.drawable.GradientDrawable();
                    bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                    bg.setCornerRadius(8f);
                    bg.setColor(dotColor);
                    dot.setBackground(bg);
                }
                tvSwipe.setTextColor(swipeColor);

                updateDots(position);

                if (isLast) {
                    View page = viewPager.findViewWithTag("page_4");
                    if (page != null) {
                        Button btn = page.findViewById(R.id.btnGetStarted);
                        if (btn != null) btn.setOnClickListener(v -> {
                            prefs.setSeenOnboarding(true);
                            startActivity(new Intent(
                                    OnboardingActivity.this, MainActivity.class));
                            finish();
                        });
                    }
                }
            }
        });

        updateDots(0);
    }

    private void updateDots(int selected) {
        float dp = getResources().getDisplayMetrics().density;
        for (int i = 0; i < dots.length; i++) {
            dots[i].setAlpha(i == selected ? 1f : 0.4f);
            android.view.ViewGroup.LayoutParams lp = dots[i].getLayoutParams();
            lp.width = (int)((i == selected ? 24 : 8) * dp);
            dots[i].setLayoutParams(lp);
        }
    }
}