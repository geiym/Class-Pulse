package com.classpulse.database;

import androidx.room.TypeConverter;

import com.classpulse.models.Subject.ClassTimeSlot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Converters {

    // ─── List<String> ─────────────────────────────────────────────────────────

    @TypeConverter
    public static String fromStringList(List<String> list) {
        if (list == null) return null;
        JSONArray arr = new JSONArray();
        for (String s : list) arr.put(s);
        return arr.toString();
    }

    @TypeConverter
    public static List<String> toStringList(String json) {
        if (json == null) return null;
        List<String> result = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) result.add(arr.getString(i));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    // ─── List<ClassTimeSlot> ──────────────────────────────────────────────────

    @TypeConverter
    public static String fromTimeSlotList(List<ClassTimeSlot> list) {
        if (list == null) return null;
        try {
            JSONArray arr = new JSONArray();
            for (ClassTimeSlot slot : list) {
                JSONObject obj = new JSONObject();
                obj.put("day",         slot.day);
                obj.put("startHour",   slot.startHour);
                obj.put("startMinute", slot.startMinute);
                obj.put("endHour",     slot.endHour);
                obj.put("endMinute",   slot.endMinute);
                arr.put(obj);
            }
            return arr.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @TypeConverter
    public static List<ClassTimeSlot> toTimeSlotList(String json) {
        if (json == null) return null;
        List<ClassTimeSlot> result = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                ClassTimeSlot slot = new ClassTimeSlot(
                        obj.getString("day"),
                        obj.getInt("startHour"),
                        obj.getInt("startMinute"),
                        obj.getInt("endHour"),
                        obj.getInt("endMinute")
                );
                result.add(slot);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}