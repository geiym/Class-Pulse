package com.classpulse.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsManager {

    private static final String PREF_NAME        = "classpulse_prefs";
    private static final String KEY_USER_NAME    = "user_name";
    private static final String KEY_FIRST_LAUNCH = "first_launch";
    private static final String KEY_BIRTHDAY     = "user_birthday";
    private static final String KEY_SCHOOL       = "user_school";
    private static final String KEY_YEAR_LEVEL   = "user_year_level";

    private final SharedPreferences prefs;

    public PrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Student");
    }

    public void setUserName(String name) {
        prefs.edit().putString(KEY_USER_NAME, name).apply();
    }

    public boolean isFirstLaunch() {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true);
    }

    public void setFirstLaunchDone() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply();
    }

    public String getBirthday() {
        return prefs.getString(KEY_BIRTHDAY, "");
    }

    public void setBirthday(String birthday) {
        prefs.edit().putString(KEY_BIRTHDAY, birthday).apply();
    }

    public String getSchool() {
        return prefs.getString(KEY_SCHOOL, "");
    }

    public void setSchool(String school) {
        prefs.edit().putString(KEY_SCHOOL, school).apply();
    }

    public String getYearLevel() {
        return prefs.getString(KEY_YEAR_LEVEL, "");
    }

    public void setYearLevel(String yearLevel) {
        prefs.edit().putString(KEY_YEAR_LEVEL, yearLevel).apply();
    }

    public void setIdCardColor(String hexColor) {
        prefs.edit().putString("id_card_color", hexColor).apply();
    }

    public String getIdCardColor() {
        return prefs.getString("id_card_color", null);
    }

    public void setPillIcon(String path) {
        prefs.edit().putString("pill_icon", path).apply();
    }

    public String getPillIcon() {
        return prefs.getString("pill_icon", null);
    }

    public void setAvatarIcon(String path) {
        prefs.edit().putString("avatar_icon", path).apply();
    }

    public String getAvatarIcon() {
        return prefs.getString("avatar_icon", null);
    }

    public boolean isReminderOn() {
        return prefs.getBoolean("reminder_on", false);
    }

    public void setReminderOn(boolean on) {
        prefs.edit().putBoolean("reminder_on", on).apply();
    }

    public String getReminderTime() {
        return prefs.getString("reminder_time", "8:00 AM");
    }

    public void setReminderTime(String time) {
        prefs.edit().putString("reminder_time", time).apply();
    }

    // ── Onboarding ──
    public boolean hasSeenOnboarding() {
        return prefs.getBoolean("seen_onboarding", false);
    }

    public void setSeenOnboarding(boolean seen) {
        prefs.edit().putBoolean("seen_onboarding", seen).apply();
    }
}