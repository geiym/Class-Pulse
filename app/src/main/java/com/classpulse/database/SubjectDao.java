package com.classpulse.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.classpulse.models.Subject;
import java.util.List;

@Dao
public interface SubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Subject subject);

    @Update
    void update(Subject subject);

    @Delete
    void delete(Subject subject);

    @Query("DELETE FROM subjects")
    void deleteAll();

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    LiveData<List<Subject>> getAllSubjects();

    @Query("SELECT * FROM subjects ORDER BY name ASC")
    List<Subject> getAllSubjectsSync();

    @Query("SELECT * FROM subjects WHERE id = :id")
    LiveData<Subject> getSubjectById(int id);

    @Query("SELECT * FROM subjects WHERE id = :id")
    Subject getSubjectByIdSync(int id);
}