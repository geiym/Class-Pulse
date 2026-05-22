package com.classpulse.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "class_logs",
    foreignKeys = @ForeignKey(
        entity = Subject.class,
        parentColumns = "id",
        childColumns = "subjectId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("subjectId")}
)
public class ClassLog {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int subjectId;
    public String subjectName;     // denormalized for easy display
    public String attendance;      // "Present", "Late", "Absent"
    public String participation;   // "Low", "Medium", "High"
    public String mood;            // "Focused", "Neutral", "Tired", "Stressed"
    public String notes;
    public long logDate;           // timestamp of the class date
    public String dayOfWeek;       // "Mon", "Tue", etc.
    public String timeSlot;        // "Morning", "Afternoon", "Evening"

    public ClassLog() {}

    public ClassLog(int subjectId, String subjectName, String attendance,
                    String participation, String mood, String notes, long logDate,
                    String dayOfWeek, String timeSlot) {
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.attendance = attendance;
        this.participation = participation;
        this.mood = mood;
        this.notes = notes;
        this.logDate = logDate;
        this.dayOfWeek = dayOfWeek;
        this.timeSlot = timeSlot;
    }

    public int getParticipationScore() {
        if ("High".equals(participation)) return 3;
        if ("Medium".equals(participation)) return 2;
        if ("Low".equals(participation)) return 1;
        return 0;
    }

    public int getAttendanceScore() {
        if ("Present".equals(attendance)) return 2;
        if ("Late".equals(attendance)) return 1;
        return 0;
    }

    public int getEngagementScore() {
        return getAttendanceScore() + getParticipationScore() + (notes != null && !notes.isEmpty() ? 1 : 0);
    }
}
