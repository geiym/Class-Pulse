package com.classpulse.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.classpulse.models.Subject;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings({"unchecked", "deprecation"})
public final class SubjectDao_Impl implements SubjectDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Subject> __insertionAdapterOfSubject;

  private final EntityDeletionOrUpdateAdapter<Subject> __deletionAdapterOfSubject;

  private final EntityDeletionOrUpdateAdapter<Subject> __updateAdapterOfSubject;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public SubjectDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSubject = new EntityInsertionAdapter<Subject>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `subjects` (`id`,`name`,`classCode`,`instructor`,`colorInt`,`iconUri`,`classDays`,`timeSlots`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Subject entity) {
        statement.bindLong(1, entity.id);
        if (entity.name == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.name);
        }
        if (entity.classCode == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.classCode);
        }
        if (entity.instructor == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.instructor);
        }
        statement.bindLong(5, entity.colorInt);
        if (entity.iconUri == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.iconUri);
        }
        final String _tmp = Converters.fromStringList(entity.classDays);
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
        final String _tmp_1 = Converters.fromTimeSlotList(entity.timeSlots);
        if (_tmp_1 == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp_1);
        }
      }
    };
    this.__deletionAdapterOfSubject = new EntityDeletionOrUpdateAdapter<Subject>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `subjects` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Subject entity) {
        statement.bindLong(1, entity.id);
      }
    };
    this.__updateAdapterOfSubject = new EntityDeletionOrUpdateAdapter<Subject>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `subjects` SET `id` = ?,`name` = ?,`classCode` = ?,`instructor` = ?,`colorInt` = ?,`iconUri` = ?,`classDays` = ?,`timeSlots` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Subject entity) {
        statement.bindLong(1, entity.id);
        if (entity.name == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.name);
        }
        if (entity.classCode == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.classCode);
        }
        if (entity.instructor == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.instructor);
        }
        statement.bindLong(5, entity.colorInt);
        if (entity.iconUri == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.iconUri);
        }
        final String _tmp = Converters.fromStringList(entity.classDays);
        if (_tmp == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp);
        }
        final String _tmp_1 = Converters.fromTimeSlotList(entity.timeSlots);
        if (_tmp_1 == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp_1);
        }
        statement.bindLong(9, entity.id);
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM subjects";
        return _query;
      }
    };
  }

  @Override
  public void insert(final Subject subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfSubject.insert(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(final Subject subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __deletionAdapterOfSubject.handle(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void update(final Subject subject) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __updateAdapterOfSubject.handle(subject);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteAll() {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAll.acquire();
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteAll.release(_stmt);
    }
  }

  @Override
  public LiveData<List<Subject>> getAllSubjects() {
    final String _sql = "SELECT * FROM subjects ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"subjects"}, false, new Callable<List<Subject>>() {
      @Override
      @Nullable
      public List<Subject> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfClassCode = CursorUtil.getColumnIndexOrThrow(_cursor, "classCode");
          final int _cursorIndexOfInstructor = CursorUtil.getColumnIndexOrThrow(_cursor, "instructor");
          final int _cursorIndexOfColorInt = CursorUtil.getColumnIndexOrThrow(_cursor, "colorInt");
          final int _cursorIndexOfIconUri = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUri");
          final int _cursorIndexOfClassDays = CursorUtil.getColumnIndexOrThrow(_cursor, "classDays");
          final int _cursorIndexOfTimeSlots = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlots");
          final List<Subject> _result = new ArrayList<Subject>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Subject _item;
            _item = new Subject();
            _item.id = _cursor.getInt(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfName)) {
              _item.name = null;
            } else {
              _item.name = _cursor.getString(_cursorIndexOfName);
            }
            if (_cursor.isNull(_cursorIndexOfClassCode)) {
              _item.classCode = null;
            } else {
              _item.classCode = _cursor.getString(_cursorIndexOfClassCode);
            }
            if (_cursor.isNull(_cursorIndexOfInstructor)) {
              _item.instructor = null;
            } else {
              _item.instructor = _cursor.getString(_cursorIndexOfInstructor);
            }
            _item.colorInt = _cursor.getInt(_cursorIndexOfColorInt);
            if (_cursor.isNull(_cursorIndexOfIconUri)) {
              _item.iconUri = null;
            } else {
              _item.iconUri = _cursor.getString(_cursorIndexOfIconUri);
            }
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfClassDays)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfClassDays);
            }
            _item.classDays = Converters.toStringList(_tmp);
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfTimeSlots)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfTimeSlots);
            }
            _item.timeSlots = Converters.toTimeSlotList(_tmp_1);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<Subject> getAllSubjectsSync() {
    final String _sql = "SELECT * FROM subjects ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfClassCode = CursorUtil.getColumnIndexOrThrow(_cursor, "classCode");
      final int _cursorIndexOfInstructor = CursorUtil.getColumnIndexOrThrow(_cursor, "instructor");
      final int _cursorIndexOfColorInt = CursorUtil.getColumnIndexOrThrow(_cursor, "colorInt");
      final int _cursorIndexOfIconUri = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUri");
      final int _cursorIndexOfClassDays = CursorUtil.getColumnIndexOrThrow(_cursor, "classDays");
      final int _cursorIndexOfTimeSlots = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlots");
      final List<Subject> _result = new ArrayList<Subject>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final Subject _item;
        _item = new Subject();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfName)) {
          _item.name = null;
        } else {
          _item.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfClassCode)) {
          _item.classCode = null;
        } else {
          _item.classCode = _cursor.getString(_cursorIndexOfClassCode);
        }
        if (_cursor.isNull(_cursorIndexOfInstructor)) {
          _item.instructor = null;
        } else {
          _item.instructor = _cursor.getString(_cursorIndexOfInstructor);
        }
        _item.colorInt = _cursor.getInt(_cursorIndexOfColorInt);
        if (_cursor.isNull(_cursorIndexOfIconUri)) {
          _item.iconUri = null;
        } else {
          _item.iconUri = _cursor.getString(_cursorIndexOfIconUri);
        }
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfClassDays)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfClassDays);
        }
        _item.classDays = Converters.toStringList(_tmp);
        final String _tmp_1;
        if (_cursor.isNull(_cursorIndexOfTimeSlots)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getString(_cursorIndexOfTimeSlots);
        }
        _item.timeSlots = Converters.toTimeSlotList(_tmp_1);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public LiveData<Subject> getSubjectById(final int id) {
    final String _sql = "SELECT * FROM subjects WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return __db.getInvalidationTracker().createLiveData(new String[] {"subjects"}, false, new Callable<Subject>() {
      @Override
      @Nullable
      public Subject call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfClassCode = CursorUtil.getColumnIndexOrThrow(_cursor, "classCode");
          final int _cursorIndexOfInstructor = CursorUtil.getColumnIndexOrThrow(_cursor, "instructor");
          final int _cursorIndexOfColorInt = CursorUtil.getColumnIndexOrThrow(_cursor, "colorInt");
          final int _cursorIndexOfIconUri = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUri");
          final int _cursorIndexOfClassDays = CursorUtil.getColumnIndexOrThrow(_cursor, "classDays");
          final int _cursorIndexOfTimeSlots = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlots");
          final Subject _result;
          if (_cursor.moveToFirst()) {
            _result = new Subject();
            _result.id = _cursor.getInt(_cursorIndexOfId);
            if (_cursor.isNull(_cursorIndexOfName)) {
              _result.name = null;
            } else {
              _result.name = _cursor.getString(_cursorIndexOfName);
            }
            if (_cursor.isNull(_cursorIndexOfClassCode)) {
              _result.classCode = null;
            } else {
              _result.classCode = _cursor.getString(_cursorIndexOfClassCode);
            }
            if (_cursor.isNull(_cursorIndexOfInstructor)) {
              _result.instructor = null;
            } else {
              _result.instructor = _cursor.getString(_cursorIndexOfInstructor);
            }
            _result.colorInt = _cursor.getInt(_cursorIndexOfColorInt);
            if (_cursor.isNull(_cursorIndexOfIconUri)) {
              _result.iconUri = null;
            } else {
              _result.iconUri = _cursor.getString(_cursorIndexOfIconUri);
            }
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfClassDays)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfClassDays);
            }
            _result.classDays = Converters.toStringList(_tmp);
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfTimeSlots)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfTimeSlots);
            }
            _result.timeSlots = Converters.toTimeSlotList(_tmp_1);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Subject getSubjectByIdSync(final int id) {
    final String _sql = "SELECT * FROM subjects WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfClassCode = CursorUtil.getColumnIndexOrThrow(_cursor, "classCode");
      final int _cursorIndexOfInstructor = CursorUtil.getColumnIndexOrThrow(_cursor, "instructor");
      final int _cursorIndexOfColorInt = CursorUtil.getColumnIndexOrThrow(_cursor, "colorInt");
      final int _cursorIndexOfIconUri = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUri");
      final int _cursorIndexOfClassDays = CursorUtil.getColumnIndexOrThrow(_cursor, "classDays");
      final int _cursorIndexOfTimeSlots = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlots");
      final Subject _result;
      if (_cursor.moveToFirst()) {
        _result = new Subject();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfName)) {
          _result.name = null;
        } else {
          _result.name = _cursor.getString(_cursorIndexOfName);
        }
        if (_cursor.isNull(_cursorIndexOfClassCode)) {
          _result.classCode = null;
        } else {
          _result.classCode = _cursor.getString(_cursorIndexOfClassCode);
        }
        if (_cursor.isNull(_cursorIndexOfInstructor)) {
          _result.instructor = null;
        } else {
          _result.instructor = _cursor.getString(_cursorIndexOfInstructor);
        }
        _result.colorInt = _cursor.getInt(_cursorIndexOfColorInt);
        if (_cursor.isNull(_cursorIndexOfIconUri)) {
          _result.iconUri = null;
        } else {
          _result.iconUri = _cursor.getString(_cursorIndexOfIconUri);
        }
        final String _tmp;
        if (_cursor.isNull(_cursorIndexOfClassDays)) {
          _tmp = null;
        } else {
          _tmp = _cursor.getString(_cursorIndexOfClassDays);
        }
        _result.classDays = Converters.toStringList(_tmp);
        final String _tmp_1;
        if (_cursor.isNull(_cursorIndexOfTimeSlots)) {
          _tmp_1 = null;
        } else {
          _tmp_1 = _cursor.getString(_cursorIndexOfTimeSlots);
        }
        _result.timeSlots = Converters.toTimeSlotList(_tmp_1);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
