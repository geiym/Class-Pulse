package com.classpulse.fragments;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageButton;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.classpulse.R;
import com.classpulse.database.AppDatabase;
import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;
import com.classpulse.utils.PrefsManager;
import com.classpulse.views.BarcodeView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private TextView tvIdName, tvIdBirthday, tvIdSchool, tvIdYear;
    private ImageView ivProfileAvatar;
    private BarcodeView barcodeView;
    private TextView tvStreakNumber, tvStreakCountBadge;
    private TextView tvBestParticipation, tvClassesLogged, tvHighStreak, tvCommonMoodEmoji;
    private TextView tvVibeTitle, tvVibeDesc;
    private ProgressBar pbVibeHappy, pbVibeNeutral, pbVibeStressed;
    private TextView tvVibeHappyPct, tvVibeNeutralPct, tvVibeStressedPct;
    private android.widget.RelativeLayout llIdTop;
    private android.widget.EditText etIdName, etIdBirthday, etIdSchool, etIdYear;
    private TextView tvLabelIcon, tvLabelColor, tvLabelAvatar;
    private boolean isEditMode = false;
    private static final int REQUEST_PICK_PILL   = 301;
    private static final int REQUEST_PICK_AVATAR = 302;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        applyCardColor();
        loadPrefsData();
        loadAnalyticsData();
    }

    private String darkenColorHex(String hex, float factor) {
        int color = Color.parseColor(hex);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= factor;
        return String.format("#%06X", (0xFFFFFF & Color.HSVToColor(hsv)));
    }

    private void bindViews(View v) {
        tvProfileName = v.findViewById(R.id.tv_profile_name);
        tvIdName = v.findViewById(R.id.tv_id_name);
        tvIdBirthday = v.findViewById(R.id.tv_id_birthday);
        tvIdSchool = v.findViewById(R.id.tv_id_school);
        tvIdYear = v.findViewById(R.id.tv_id_year);
        ivProfileAvatar = v.findViewById(R.id.iv_profile_avatar);
        barcodeView = v.findViewById(R.id.barcode_view);
        tvStreakNumber = v.findViewById(R.id.tv_streak_number);
        tvStreakCountBadge = v.findViewById(R.id.tv_streak_count_badge);
        tvBestParticipation = v.findViewById(R.id.tv_best_participation);
        tvClassesLogged = v.findViewById(R.id.tv_classes_logged);
        tvHighStreak = v.findViewById(R.id.tv_high_streak);
        tvCommonMoodEmoji = v.findViewById(R.id.tv_common_mood_emoji);
        tvVibeTitle = v.findViewById(R.id.tv_vibe_title);
        tvVibeDesc = v.findViewById(R.id.tv_vibe_desc);
        pbVibeHappy = v.findViewById(R.id.pb_vibe_happy);
        pbVibeNeutral = v.findViewById(R.id.pb_vibe_neutral);
        pbVibeStressed = v.findViewById(R.id.pb_vibe_stressed);
        tvVibeHappyPct = v.findViewById(R.id.tv_vibe_happy_pct);
        tvVibeNeutralPct = v.findViewById(R.id.tv_vibe_neutral_pct);
        tvVibeStressedPct = v.findViewById(R.id.tv_vibe_stressed_pct);
        llIdTop = v.findViewById(R.id.ll_id_top);
        etIdName = v.findViewById(R.id.et_id_name);
        etIdBirthday = v.findViewById(R.id.et_id_birthday);
        etIdSchool = v.findViewById(R.id.et_id_school);
        etIdYear = v.findViewById(R.id.et_id_year);
        tvLabelIcon = v.findViewById(R.id.tv_label_icon);
        tvLabelAvatar = v.findViewById(R.id.tv_label_avatar);

        ImageView ivIdIcon = v.findViewById(R.id.iv_id_icon);
        if (ivIdIcon != null) {
            ivIdIcon.setOutlineProvider(new android.view.ViewOutlineProvider() {
                @Override
                public void getOutline(View view, android.graphics.Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            ivIdIcon.setClipToOutline(true);
        }

        ImageButton btnEdit = v.findViewById(R.id.btn_edit_id);
        ImageButton btnSave = v.findViewById(R.id.btn_save_id);
        if (btnEdit != null) btnEdit.setOnClickListener(vv -> enterEditMode(btnSave));
        if (btnSave != null) btnSave.setOnClickListener(vv -> saveEditMode(btnSave));

        View colorDot = v.findViewById(R.id.view_color_dot_profile);
        if (colorDot != null) colorDot.setOnClickListener(vv -> {
            if (isEditMode) showColorPickerModal();
        });

        ImageButton btnSettings = v.findViewById(R.id.btn_settings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(vv -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragment_container, new com.classpulse.fragments.SettingsFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    private void enterEditMode(ImageButton btnSave) {
        isEditMode = true;

        tvIdName.setVisibility(View.GONE);
        tvIdBirthday.setVisibility(View.GONE);
        tvIdSchool.setVisibility(View.GONE);
        tvIdYear.setVisibility(View.GONE);

        etIdName.setVisibility(View.VISIBLE);
        etIdBirthday.setVisibility(View.VISIBLE);
        etIdSchool.setVisibility(View.VISIBLE);
        etIdYear.setVisibility(View.VISIBLE);

        PrefsManager prefs = new PrefsManager(requireContext());
        etIdName.setText(prefs.getUserName());
        etIdBirthday.setText(prefs.getBirthday());
        etIdSchool.setText(prefs.getSchool());
        etIdYear.setText(prefs.getYearLevel());

        tvLabelIcon.setVisibility(View.VISIBLE);
        tvLabelAvatar.setVisibility(View.VISIBLE);

        if (btnSave != null) btnSave.setVisibility(View.VISIBLE);

        if (ivProfileAvatar != null)
            ivProfileAvatar.setOnClickListener(vv -> showAvatarPicker());
        ImageView ivIdIcon = requireView().findViewById(R.id.iv_id_icon);
        if (ivIdIcon != null)
            ivIdIcon.setOnClickListener(vv -> showPillIconPicker());

        View colorDot = requireView().findViewById(R.id.view_color_dot_profile);
        if (colorDot != null) {
            colorDot.setVisibility(View.VISIBLE);
            PrefsManager p = new PrefsManager(requireContext());
            String savedColor = p.getIdCardColor();
            if (savedColor == null || savedColor.isEmpty()) savedColor = "#FCE4EC";
            int c = Color.parseColor(savedColor);
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(c);
            float[] hsv = new float[3];
            Color.colorToHSV(c, hsv);
            hsv[1] = Math.min(hsv[1] + 0.3f, 1f);
            hsv[2] *= 0.55f;
            dotBg.setStroke((int)(3 * getResources().getDisplayMetrics().density), Color.HSVToColor(hsv));
            colorDot.setBackground(dotBg);
        }
    }

    private void saveEditMode(ImageButton btnSave) {
        isEditMode = false;

        PrefsManager prefs = new PrefsManager(requireContext());
        String newName     = etIdName.getText().toString().trim();
        String newBirthday = etIdBirthday.getText().toString().trim();
        String newSchool   = etIdSchool.getText().toString().trim();
        String newYear     = etIdYear.getText().toString().trim();

        prefs.setUserName(newName);
        prefs.setBirthday(newBirthday);
        prefs.setSchool(newSchool);
        prefs.setYearLevel(newYear);

        tvIdName.setText(newName);
        tvIdBirthday.setText(newBirthday);
        tvIdSchool.setText(newSchool);
        tvIdYear.setText(newYear);
        if (tvProfileName != null) tvProfileName.setText(newName);

        tvIdName.setVisibility(View.VISIBLE);
        tvIdBirthday.setVisibility(View.VISIBLE);
        tvIdSchool.setVisibility(View.VISIBLE);
        tvIdYear.setVisibility(View.VISIBLE);
        etIdName.setVisibility(View.GONE);
        etIdBirthday.setVisibility(View.GONE);
        etIdSchool.setVisibility(View.GONE);
        etIdYear.setVisibility(View.GONE);

        tvLabelIcon.setVisibility(View.GONE);
        tvLabelAvatar.setVisibility(View.GONE);
        if (btnSave != null) btnSave.setVisibility(View.GONE);

        if (ivProfileAvatar != null) ivProfileAvatar.setOnClickListener(null);
        ImageView ivIdIcon = requireView().findViewById(R.id.iv_id_icon);
        if (ivIdIcon != null) ivIdIcon.setOnClickListener(null);

        // ── Safe color apply — never call parseColor on null ──────────────────
        String savedColor = prefs.getIdCardColor();
        if (savedColor == null || savedColor.isEmpty()) {
            savedColor = "#FCE4EC";
        }

        if (llIdTop != null) {
            llIdTop.setBackgroundColor(Color.parseColor(savedColor));
        }

        View colorDot = requireView().findViewById(R.id.view_color_dot_profile);
        if (colorDot != null && colorDot.getBackground() != null) {
            colorDot.getBackground().mutate().setTint(Color.parseColor(savedColor));
            colorDot.setVisibility(View.GONE);
        }

        android.widget.Toast.makeText(requireContext(), "Saved!", android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != android.app.Activity.RESULT_OK || data == null || data.getData() == null) return;
        android.net.Uri uri = data.getData();
        try {
            requireContext().getContentResolver().takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        PrefsManager prefs = new PrefsManager(requireContext());
        if (requestCode == REQUEST_PICK_AVATAR) {
            prefs.setAvatarIcon(uri.toString());
            prefs.setPillIcon(uri.toString()); // keep top-left in sync
            if (ivProfileAvatar != null) ivProfileAvatar.setImageURI(uri);
            ImageView ivIdIcon = requireView().findViewById(R.id.iv_id_icon);
            if (ivIdIcon != null) ivIdIcon.setImageURI(uri); // update top-left too
        } else if (requestCode == REQUEST_PICK_PILL) {
            prefs.setPillIcon(uri.toString());
            ImageView ivIdIcon = requireView().findViewById(R.id.iv_id_icon);
            if (ivIdIcon != null) ivIdIcon.setImageURI(uri);
        }
    }

    private void showColorPickerModal() {
        String[] colors = {"#FCE4EC","#E3F2FD","#E8F5E9","#FFF9C4","#EDE7F6","#FBE9E7"};
        int[] colorInts = new int[colors.length];
        for (int i = 0; i < colors.length; i++)
            colorInts[i] = Color.parseColor(colors[i]);

        String savedColor = new PrefsManager(requireContext()).getIdCardColor();
        if (savedColor == null || savedColor.isEmpty()) savedColor = "#FCE4EC";

        ColorPickerBottomSheet sheet = ColorPickerBottomSheet.newInstance(
                colorInts, Color.parseColor(savedColor));
        sheet.setOnColorSelectedListener(color -> {
            new PrefsManager(requireContext()).setIdCardColor(
                    String.format("#%06X", (0xFFFFFF & color)));
            if (llIdTop != null) llIdTop.setBackgroundColor(color);
            View colorDot = requireView().findViewById(R.id.view_color_dot_profile);
            if (colorDot != null && colorDot.getBackground() != null)
                colorDot.getBackground().mutate().setTint(color);
        });
        sheet.show(getParentFragmentManager(), "color_picker");
    }

    private void applyCardColor() {
        if (llIdTop == null) return;
        PrefsManager prefs = new PrefsManager(requireContext());
        String savedColor = prefs.getIdCardColor();
        if (savedColor == null || savedColor.isEmpty()) savedColor = "#FCE4EC";
        int color = Color.parseColor(savedColor);
        llIdTop.setBackgroundColor(color);

        View colorDot = getView() != null ? getView().findViewById(R.id.view_color_dot_profile) : null;
        if (colorDot != null) {
            GradientDrawable dotBg = new GradientDrawable();
            dotBg.setShape(GradientDrawable.OVAL);
            dotBg.setColor(color);
            float[] hsv = new float[3];
            Color.colorToHSV(color, hsv);
            hsv[2] *= 0.6f;
            dotBg.setStroke((int)(2 * getResources().getDisplayMetrics().density), Color.HSVToColor(hsv));
            colorDot.setBackground(dotBg);
        }
    }

    private void loadPrefsData() {
        PrefsManager prefs = new PrefsManager(requireContext());
        String name = prefs.getUserName();
        if (tvProfileName != null) tvProfileName.setText(name);
        if (tvIdName      != null) tvIdName.setText(name);
        if (tvIdBirthday  != null) tvIdBirthday.setText(prefs.getBirthday());
        if (tvIdSchool    != null) tvIdSchool.setText(prefs.getSchool());
        if (tvIdYear      != null) tvIdYear.setText(prefs.getYearLevel());
        if (barcodeView   != null) barcodeView.setData(name);

        String avatarUri = prefs.getAvatarIcon();
        ImageView ivIdIcon = getView() != null ? getView().findViewById(R.id.iv_id_icon) : null;

        if (avatarUri != null) {
            android.net.Uri parsedUri = android.net.Uri.parse(avatarUri);
            // Update main ID photo
            if (ivProfileAvatar != null) {
                try { ivProfileAvatar.setImageURI(parsedUri); }
                catch (Exception e) { ivProfileAvatar.setImageResource(R.drawable.ic_default_avatar); }
            }
            // Update top-left circle icon to match
            if (ivIdIcon != null) {
                try { ivIdIcon.setImageURI(parsedUri); }
                catch (Exception e) { ivIdIcon.setImageResource(R.drawable.ic_default_avatar); }
            }
        }
    }

    private void loadAnalyticsData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ClassLog> logs     = db.classLogDao().getAllLogsSync();
            List<Subject>  subjects = db.subjectDao().getAllSubjectsSync();

            int streak      = computeStreak(logs);
            int bestPart    = computeBestParticipation(logs);
            int totalLogged = logs.size();
            int highRow     = computeHighInARow(logs);
            String topMood  = computeTopMood(logs);

            Map<String, Integer> moodMap  = computeMoodMap(logs);
            int total         = logs.size();
            int happyCount    = moodMap.getOrDefault("Happy",    moodMap.getOrDefault("Focused", 0));
            int neutralCount  = moodMap.getOrDefault("Neutral",  0);
            int stressedCount = moodMap.getOrDefault("Stressed", 0);
            int happyPct    = total > 0 ? (int) Math.round(happyCount    * 100.0 / total) : 0;
            int neutralPct  = total > 0 ? (int) Math.round(neutralCount  * 100.0 / total) : 0;
            int stressedPct = total > 0 ? (int) Math.round(stressedCount * 100.0 / total) : 0;

            String vibeTitle = getVibeTitle(happyPct, stressedPct);
            String vibeDesc  = getVibeDesc(vibeTitle);

            requireActivity().runOnUiThread(() -> {
                if (!isAdded()) return;
                if (tvStreakNumber      != null) tvStreakNumber.setText(String.valueOf(streak));
                if (tvStreakCountBadge  != null) tvStreakCountBadge.setText(String.valueOf(streak));
                if (tvBestParticipation != null) tvBestParticipation.setText(bestPart + "%");
                if (tvClassesLogged    != null) tvClassesLogged.setText(String.valueOf(totalLogged));
                if (tvHighStreak       != null) tvHighStreak.setText(highRow + "×");
                if (tvCommonMoodEmoji  != null) tvCommonMoodEmoji.setText(getMoodEmoji(topMood));
                if (tvVibeTitle        != null) tvVibeTitle.setText(vibeTitle);
                if (tvVibeDesc         != null) tvVibeDesc.setText(vibeDesc);
                if (pbVibeHappy        != null) pbVibeHappy.setProgress(happyPct);
                if (pbVibeNeutral      != null) pbVibeNeutral.setProgress(neutralPct);
                if (pbVibeStressed     != null) pbVibeStressed.setProgress(stressedPct);
                if (tvVibeHappyPct     != null) tvVibeHappyPct.setText(happyPct + "%");
                if (tvVibeNeutralPct   != null) tvVibeNeutralPct.setText(neutralPct + "%");
                if (tvVibeStressedPct  != null) tvVibeStressedPct.setText(stressedPct + "%");
            });
        });
    }

    private int computeStreak(List<ClassLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;
        long today = getDayStart(System.currentTimeMillis());
        int streak = 0;
        long check = today;
        while (true) {
            long dayS = check, dayE = check + 86400000L;
            boolean found = false;
            for (ClassLog l : logs) if (l.logDate >= dayS && l.logDate < dayE) { found = true; break; }
            if (!found) break;
            streak++; check -= 86400000L;
        }
        return streak;
    }

    private int computeBestParticipation(List<ClassLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;
        int high = 0;
        for (ClassLog l : logs) if ("High".equals(l.participation)) high++;
        return (int) Math.round(high * 100.0 / logs.size());
    }

    private int computeHighInARow(List<ClassLog> logs) {
        int max = 0, curr = 0;
        if (logs == null) return 0;
        for (ClassLog l : logs) {
            if ("High".equals(l.participation)) { curr++; max = Math.max(max, curr); } else curr = 0;
        }
        return max;
    }

    private String computeTopMood(List<ClassLog> logs) {
        Map<String, Integer> counts = computeMoodMap(logs);
        String top = "Happy"; int best = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet())
            if (e.getValue() > best) { best = e.getValue(); top = e.getKey(); }
        return top;
    }

    private Map<String, Integer> computeMoodMap(List<ClassLog> logs) {
        Map<String, Integer> m = new HashMap<>();
        if (logs == null) return m;
        for (ClassLog l : logs) if (l.mood != null) m.put(l.mood, m.getOrDefault(l.mood, 0) + 1);
        return m;
    }

    private String getVibeTitle(int happyPct, int stressedPct) {
        if (happyPct >= 50)    return "The Enthusiast";
        if (stressedPct >= 40) return "The Overthinker";
        if (happyPct >= 30)    return "The Steady One";
        return "The Explorer";
    }

    private String getVibeDesc(String title) {
        switch (title) {
            case "The Enthusiast":  return "You're mostly happy & engaged in class!";
            case "The Overthinker": return "You tend to feel stressed — take it easy!";
            case "The Steady One":  return "You're calm and consistent in class.";
            default:                return "Your class vibe is still forming — keep logging!";
        }
    }

    private String getMoodEmoji(String mood) {
        switch (mood) {
            case "Happy": case "Focused": return "😊";
            case "Neutral":               return "😐";
            case "Stressed":              return "😰";
            case "Tired":                 return "😴";
            default:                      return "😊";
        }
    }

    private long getDayStart(long ts) {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTimeInMillis(ts);
        c.set(java.util.Calendar.HOUR_OF_DAY, 0); c.set(java.util.Calendar.MINUTE, 0);
        c.set(java.util.Calendar.SECOND, 0);       c.set(java.util.Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private void showAvatarPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_AVATAR);
    }

    private void showPillIconPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PICK_PILL);
    }

    @Override public void onResume()  { super.onResume();  loadPrefsData(); loadAnalyticsData(); applyCardColor(); }
    @Override public void onDestroy() { super.onDestroy(); executor.shutdown(); }
}