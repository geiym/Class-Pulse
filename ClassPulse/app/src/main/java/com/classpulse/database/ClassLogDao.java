package com.classpulse.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.classpulse.models.ClassLog;

import java.util.List;

@Dao
public interface ClassLogDao {

    @Insert
    long insert(ClassLog log);

    @Query("SELECT * FROM class_logs ORDER BY logDate DESC")
    LiveData<List<ClassLog>> getAllLogs();

    @Query("SELECT * FROM class_logs ORDER BY logDate DESC")
    List<ClassLog> getAllLogsSync();

    @Query("SELECT * FROM class_logs WHERE subjectId = :subjectId ORDER BY logDate DESC")
    List<ClassLog> getLogsBySubject(int subjectId);

    @Query("SELECT * FROM class_logs WHERE logDate >= :startTime AND logDate <= :endTime ORDER BY logDate DESC")
    List<ClassLog> getLogsInRange(long startTime, long endTime);

    @Query("SELECT * FROM class_logs WHERE subjectId = :subjectId AND logDate >= :startTime ORDER BY logDate DESC")
    List<ClassLog> getRecentLogsBySubject(int subjectId, long startTime);

    // For dashboard stats
    @Query("SELECT COUNT(*) FROM class_logs WHERE logDate >= :startTime")
    int getLogCountSince(long startTime);

    @Query("SELECT COUNT(*) FROM class_logs WHERE attendance = 'Present' AND logDate >= :startTime")
    int getPresentCountSince(long startTime);

    @Query("SELECT COUNT(*) FROM class_logs WHERE attendance != 'Absent' AND logDate >= :startTime")
    int getNonAbsentCountSince(long startTime);

    // Check if a log exists for a subject on a given date
    @Query("SELECT * FROM class_logs WHERE subjectId = :subjectId AND logDate >= :dayStart AND logDate <= :dayEnd LIMIT 1")
    ClassLog getLogForSubjectOnDate(int subjectId, long dayStart, long dayEnd);

    @Query("SELECT * FROM class_logs WHERE logDate >= :dayStart AND logDate <= :dayEnd")
    List<ClassLog> getLogsForDate(long dayStart, long dayEnd);

    @Query("DELETE FROM class_logs")
    void deleteAll();

    // Trend analysis queries
    @Query("SELECT * FROM class_logs WHERE dayOfWeek = :day ORDER BY logDate DESC")
    List<ClassLog> getLogsByDay(String day);

    @Query("SELECT * FROM class_logs WHERE timeSlot = :slot ORDER BY logDate DESC")
    List<ClassLog> getLogsByTimeSlot(String slot);

    @Query("SELECT * FROM class_logs WHERE dayOfWeek = :day AND timeSlot = :slot")
    List<ClassLog> getLogsByDayAndSlot(String day, String slot);

    @Query("SELECT * FROM class_logs WHERE mood = :mood")
    List<ClassLog> getLogsByMood(String mood);

    // For trajectory: last 14 days vs previous 14 days
    @Query("SELECT * FROM class_logs WHERE logDate >= :startTime ORDER BY logDate ASC")
    List<ClassLog> getLogsSince(long startTime);

    @Query("SELECT COUNT(*) FROM class_logs WHERE notes != '' AND notes IS NOT NULL AND logDate >= :since")
    int getNotesCountSince(long since);

    @Query("SELECT * FROM class_logs WHERE subjectId = :subjectId ORDER BY logDate ASC")
    List<ClassLog> getLogsForSubject(int subjectId);
}
