package com.classpulse.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.classpulse.R;
import com.classpulse.fragments.HomeFragment;
import com.classpulse.fragments.SubjectsFragment;
import com.classpulse.fragments.LogsFragment;
import com.classpulse.fragments.TrendsFragment;
import com.classpulse.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private View[] navItems;
    private FrameLayout[] navBubbles;
    private ImageView[] navFlats;
    private int currentIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(Color.parseColor("#5A8BD0"));

        navItems = new View[]{
                findViewById(R.id.nav_item_1),
                findViewById(R.id.nav_item_2),
                findViewById(R.id.nav_item_3),
                findViewById(R.id.nav_item_4),
                findViewById(R.id.nav_item_5)
        };

        navBubbles = new FrameLayout[]{
                findViewById(R.id.nav_bubble_1),
                findViewById(R.id.nav_bubble_2),
                findViewById(R.id.nav_bubble_3),
                findViewById(R.id.nav_bubble_4),
                findViewById(R.id.nav_bubble_5)
        };

        navFlats = new ImageView[]{
                findViewById(R.id.nav_icon_1_flat),
                findViewById(R.id.nav_icon_2_flat),
                findViewById(R.id.nav_icon_3_flat),
                findViewById(R.id.nav_icon_4_flat),
                findViewById(R.id.nav_icon_5_flat)
        };

        for (int i = 0; i < navItems.length; i++) {
            final int index = i;
            navItems[i].setOnClickListener(v -> navigateToIndex(index));
        }

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment(), false);
            setActiveTab(0);
        }

        handleIntentExtras();
    }

    // Called by bottom nav bar taps — clears back stack, no back button
    private void navigateToIndex(int index) {
        if (index == currentIndex) return;
        currentIndex = index;
        setActiveTab(index);

        Fragment fragment;
        switch (index) {
            case 0:  fragment = new HomeFragment(); break;
            case 1:  fragment = new SubjectsFragment(); break;
            case 2:  fragment = TrendsFragment.newInstance(false); break;
            case 3:  fragment = LogsFragment.newInstance(false); break;
            default: fragment = new ProfileFragment(); break;
        }

        getSupportFragmentManager().popBackStack(null,
                FragmentManager.POP_BACK_STACK_INCLUSIVE);
        loadFragment(fragment, false);
    }

    // Called from HomeFragment quick-action buttons — adds to back stack, shows back button
    public void navigateTo(int index) {
        Fragment fragment;
        switch (index) {
            case 2:  fragment = TrendsFragment.newInstance(true); break;
            case 3:  fragment = LogsFragment.newInstance(true); break;
            case 1:  fragment = new SubjectsFragment(); break;
            default: return;
        }

        // Update nav highlight without clearing back stack
        setActiveTab(index);

        // Load WITH back stack so pressing back returns to Home
        loadFragment(fragment, true);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            // Restore Home tab highlight when popping back
            currentIndex = 0;
            setActiveTab(0);
        } else {
            super.onBackPressed();
        }
    }

    private void setActiveTab(int index) {
        for (int i = 0; i < navBubbles.length; i++) {
            if (i == index) {
                navBubbles[i].setVisibility(View.VISIBLE);
                navFlats[i].setVisibility(View.GONE);
                navBubbles[i].setTranslationY(0f);
            } else {
                navBubbles[i].setVisibility(View.INVISIBLE);
                navFlats[i].setVisibility(View.VISIBLE);
                navBubbles[i].setTranslationY(0f);
            }
        }
    }

    private void handleIntentExtras() {
        if (getIntent() == null) return;
        String target = getIntent().getStringExtra("navigate_to");
        if (target == null) return;
        switch (target) {
            case "logs":     navigateTo(3); break;
            case "trends":   navigateTo(2); break;
            case "subjects": navigateTo(1); break;
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        androidx.fragment.app.FragmentTransaction tx =
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, fragment);
        if (addToBackStack) tx.addToBackStack(null);
        tx.commit();
    }
}