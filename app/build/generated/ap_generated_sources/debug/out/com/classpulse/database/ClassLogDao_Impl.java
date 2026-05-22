package com.classpulse.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.classpulse.models.ClassLog;
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
public final class ClassLogDao_Impl implements ClassLogDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ClassLog> __insertionAdapterOfClassLog;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAll;

  public ClassLogDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfClassLog = new EntityInsertionAdapter<ClassLog>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `class_logs` (`id`,`subjectId`,`subjectName`,`attendance`,`participation`,`mood`,`notes`,`logDate`,`dayOfWeek`,`timeSlot`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final ClassLog entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.subjectId);
        if (entity.subjectName == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.subjectName);
        }
        if (entity.attendance == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.attendance);
        }
        if (entity.participation == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.participation);
        }
        if (entity.mood == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.mood);
        }
        if (entity.notes == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.notes);
        }
        statement.bindLong(8, entity.logDate);
        if (entity.dayOfWeek == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.dayOfWeek);
        }
        if (entity.timeSlot == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.timeSlot);
        }
      }
    };
    this.__preparedStmtOfDeleteAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM class_logs";
        return _query;
      }
    };
  }

  @Override
  public long insert(final ClassLog log) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfClassLog.insertAndReturnId(log);
      __db.setTransactionSuccessful();
      return _result;
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
  public LiveData<List<ClassLog>> getAllLogs() {
    final String _sql = "SELECT * FROM class_logs ORDER BY logDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return __db.getInvalidationTracker().createLiveData(new String[] {"class_logs"}, false, new Callable<List<ClassLog>>() {
      @Override
      @Nullable
      public List<ClassLog> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
          final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
          final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
          final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
          final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
          final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
          final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
          final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ClassLog _item;
            _item = new ClassLog();
            _item.id = _cursor.getInt(_cursorIndexOfId);
            _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
            if (_cursor.isNull(_cursorIndexOfSubjectName)) {
              _item.subjectName = null;
            } else {
              _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
            }
            if (_cursor.isNull(_cursorIndexOfAttendance)) {
              _item.attendance = null;
            } else {
              _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
            }
            if (_cursor.isNull(_cursorIndexOfParticipation)) {
              _item.participation = null;
            } else {
              _item.participation = _cursor.getString(_cursorIndexOfParticipation);
            }
            if (_cursor.isNull(_cursorIndexOfMood)) {
              _item.mood = null;
            } else {
              _item.mood = _cursor.getString(_cursorIndexOfMood);
            }
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _item.notes = null;
            } else {
              _item.notes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
            if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
              _item.dayOfWeek = null;
            } else {
              _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
            }
            if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
              _item.timeSlot = null;
            } else {
              _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
            }
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
  public List<ClassLog> getAllLogsSync() {
    final String _sql = "SELECT * FROM class_logs ORDER BY logDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsBySubject(final int subjectId) {
    final String _sql = "SELECT * FROM class_logs WHERE subjectId = ? ORDER BY logDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, subjectId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsInRange(final long startTime, final long endTime) {
    final String _sql = "SELECT * FROM class_logs WHERE logDate >= ? AND logDate <= ? ORDER BY logDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getRecentLogsBySubject(final int subjectId, final long startTime) {
    final String _sql = "SELECT * FROM class_logs WHERE subjectId = ? AND logDate >= ? ORDER BY logDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, subjectId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getLogCountSince(final long startTime) {
    final String _sql = "SELECT COUNT(*) FROM class_logs WHERE logDate >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getPresentCountSince(final long startTime) {
    final String _sql = "SELECT COUNT(*) FROM class_logs WHERE attendance = 'Present' AND logDate >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getNonAbsentCountSince(final long startTime) {
    final String _sql = "SELECT COUNT(*) FROM class_logs WHERE attendance != 'Absent' AND logDate >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public ClassLog getLogForSubjectOnDate(final int subjectId, final long dayStart,
      final long dayEnd) {
    final String _sql = "SELECT * FROM class_logs WHERE subjectId = ? AND logDate >= ? AND logDate <= ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, subjectId);
    _argIndex = 2;
    _statement.bindLong(_argIndex, dayStart);
    _argIndex = 3;
    _statement.bindLong(_argIndex, dayEnd);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final ClassLog _result;
      if (_cursor.moveToFirst()) {
        _result = new ClassLog();
        _result.id = _cursor.getInt(_cursorIndexOfId);
        _result.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _result.subjectName = null;
        } else {
          _result.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _result.attendance = null;
        } else {
          _result.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _result.participation = null;
        } else {
          _result.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _result.mood = null;
        } else {
          _result.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _result.notes = null;
        } else {
          _result.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _result.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _result.dayOfWeek = null;
        } else {
          _result.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _result.timeSlot = null;
        } else {
          _result.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsForDate(final long dayStart, final long dayEnd) {
    final String _sql = "SELECT * FROM class_logs WHERE logDate >= ? AND logDate <= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayStart);
    _argIndex = 2;
    _statement.bindLong(_argIndex, dayEnd);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsByDay(final String day) {
    final String _sql = "SELECT * FROM class_logs WHERE dayOfWeek = ? ORDER BY logDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (day == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, day);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsByTimeSlot(final String slot) {
    final String _sql = "SELECT * FROM class_logs WHERE timeSlot = ? ORDER BY logDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (slot == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, slot);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsByDayAndSlot(final String day, final String slot) {
    final String _sql = "SELECT * FROM class_logs WHERE dayOfWeek = ? AND timeSlot = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (day == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, day);
    }
    _argIndex = 2;
    if (slot == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, slot);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsByMood(final String mood) {
    final String _sql = "SELECT * FROM class_logs WHERE mood = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (mood == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, mood);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsSince(final long startTime) {
    final String _sql = "SELECT * FROM class_logs WHERE logDate >= ? ORDER BY logDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public int getNotesCountSince(final long since) {
    final String _sql = "SELECT COUNT(*) FROM class_logs WHERE notes != '' AND notes IS NOT NULL AND logDate >= ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, since);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _result;
      if (_cursor.moveToFirst()) {
        _result = _cursor.getInt(0);
      } else {
        _result = 0;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @Override
  public List<ClassLog> getLogsForSubject(final int subjectId) {
    final String _sql = "SELECT * FROM class_logs WHERE subjectId = ? ORDER BY logDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, subjectId);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfSubjectId = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectId");
      final int _cursorIndexOfSubjectName = CursorUtil.getColumnIndexOrThrow(_cursor, "subjectName");
      final int _cursorIndexOfAttendance = CursorUtil.getColumnIndexOrThrow(_cursor, "attendance");
      final int _cursorIndexOfParticipation = CursorUtil.getColumnIndexOrThrow(_cursor, "participation");
      final int _cursorIndexOfMood = CursorUtil.getColumnIndexOrThrow(_cursor, "mood");
      final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
      final int _cursorIndexOfLogDate = CursorUtil.getColumnIndexOrThrow(_cursor, "logDate");
      final int _cursorIndexOfDayOfWeek = CursorUtil.getColumnIndexOrThrow(_cursor, "dayOfWeek");
      final int _cursorIndexOfTimeSlot = CursorUtil.getColumnIndexOrThrow(_cursor, "timeSlot");
      final List<ClassLog> _result = new ArrayList<ClassLog>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ClassLog _item;
        _item = new ClassLog();
        _item.id = _cursor.getInt(_cursorIndexOfId);
        _item.subjectId = _cursor.getInt(_cursorIndexOfSubjectId);
        if (_cursor.isNull(_cursorIndexOfSubjectName)) {
          _item.subjectName = null;
        } else {
          _item.subjectName = _cursor.getString(_cursorIndexOfSubjectName);
        }
        if (_cursor.isNull(_cursorIndexOfAttendance)) {
          _item.attendance = null;
        } else {
          _item.attendance = _cursor.getString(_cursorIndexOfAttendance);
        }
        if (_cursor.isNull(_cursorIndexOfParticipation)) {
          _item.participation = null;
        } else {
          _item.participation = _cursor.getString(_cursorIndexOfParticipation);
        }
        if (_cursor.isNull(_cursorIndexOfMood)) {
          _item.mood = null;
        } else {
          _item.mood = _cursor.getString(_cursorIndexOfMood);
        }
        if (_cursor.isNull(_cursorIndexOfNotes)) {
          _item.notes = null;
        } else {
          _item.notes = _cursor.getString(_cursorIndexOfNotes);
        }
        _item.logDate = _cursor.getLong(_cursorIndexOfLogDate);
        if (_cursor.isNull(_cursorIndexOfDayOfWeek)) {
          _item.dayOfWeek = null;
        } else {
          _item.dayOfWeek = _cursor.getString(_cursorIndexOfDayOfWeek);
        }
        if (_cursor.isNull(_cursorIndexOfTimeSlot)) {
          _item.timeSlot = null;
        } else {
          _item.timeSlot = _cursor.getString(_cursorIndexOfTimeSlot);
        }
        _result.add(_item);
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
