package com.classpulse.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.classpulse.database.AppDatabase;
import com.classpulse.database.SubjectDao;
import com.classpulse.models.Subject;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubjectViewModel extends AndroidViewModel {

    private final SubjectDao dao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SubjectViewModel(@NonNull Application application) {
        super(application);
        dao = AppDatabase.getInstance(application).subjectDao();
    }

    public LiveData<List<Subject>> getAllSubjects() {
        return dao.getAllSubjects();
    }

    public LiveData<Subject> getSubjectById(int id) {
        return dao.getSubjectById(id);
    }

    public void insert(Subject subject) {
        executor.execute(() -> dao.insert(subject));
    }

    public void update(Subject subject) {
        executor.execute(() -> dao.update(subject));
    }

    public void delete(Subject subject) {
        executor.execute(() -> dao.delete(subject));
    }
}