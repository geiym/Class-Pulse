package com.classpulse.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.classpulse.models.ClassLog;
import com.classpulse.models.Subject;

@Database(entities = {Subject.class, ClassLog.class}, version = 3, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static final String DB_NAME = "classpulse_db";
    private static volatile AppDatabase instance;

    public abstract SubjectDao subjectDao();
    public abstract ClassLogDao classLogDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DB_NAME
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return instance;
    }
}