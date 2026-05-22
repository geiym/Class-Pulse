package com.classpulse.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile SubjectDao _subjectDao;

  private volatile ClassLogDao _classLogDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `subjects` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `classCode` TEXT, `instructor` TEXT, `colorInt` INTEGER NOT NULL, `iconUri` TEXT, `classDays` TEXT, `timeSlots` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `class_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `subjectId` INTEGER NOT NULL, `subjectName` TEXT, `attendance` TEXT, `participation` TEXT, `mood` TEXT, `notes` TEXT, `logDate` INTEGER NOT NULL, `dayOfWeek` TEXT, `timeSlot` TEXT, FOREIGN KEY(`subjectId`) REFERENCES `subjects`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )");
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_class_logs_subjectId` ON `class_logs` (`subjectId`)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '8d2db0ee9fa0ca56122408f11a2b78b3')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `subjects`");
        db.execSQL("DROP TABLE IF EXISTS `class_logs`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsSubjects = new HashMap<String, TableInfo.Column>(8);
        _columnsSubjects.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("classCode", new TableInfo.Column("classCode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("instructor", new TableInfo.Column("instructor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("colorInt", new TableInfo.Column("colorInt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("iconUri", new TableInfo.Column("iconUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("classDays", new TableInfo.Column("classDays", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSubjects.put("timeSlots", new TableInfo.Column("timeSlots", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSubjects = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSubjects = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSubjects = new TableInfo("subjects", _columnsSubjects, _foreignKeysSubjects, _indicesSubjects);
        final TableInfo _existingSubjects = TableInfo.read(db, "subjects");
        if (!_infoSubjects.equals(_existingSubjects)) {
          return new RoomOpenHelper.ValidationResult(false, "subjects(com.classpulse.models.Subject).\n"
                  + " Expected:\n" + _infoSubjects + "\n"
                  + " Found:\n" + _existingSubjects);
        }
        final HashMap<String, TableInfo.Column> _columnsClassLogs = new HashMap<String, TableInfo.Column>(10);
        _columnsClassLogs.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("subjectId", new TableInfo.Column("subjectId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("subjectName", new TableInfo.Column("subjectName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("attendance", new TableInfo.Column("attendance", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("participation", new TableInfo.Column("participation", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("mood", new TableInfo.Column("mood", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("logDate", new TableInfo.Column("logDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("dayOfWeek", new TableInfo.Column("dayOfWeek", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsClassLogs.put("timeSlot", new TableInfo.Column("timeSlot", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysClassLogs = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysClassLogs.add(new TableInfo.ForeignKey("subjects", "CASCADE", "NO ACTION", Arrays.asList("subjectId"), Arrays.asList("id")));
        final HashSet<TableInfo.Index> _indicesClassLogs = new HashSet<TableInfo.Index>(1);
        _indicesClassLogs.add(new TableInfo.Index("index_class_logs_subjectId", false, Arrays.asList("subjectId"), Arrays.asList("ASC")));
        final TableInfo _infoClassLogs = new TableInfo("class_logs", _columnsClassLogs, _foreignKeysClassLogs, _indicesClassLogs);
        final TableInfo _existingClassLogs = TableInfo.read(db, "class_logs");
        if (!_infoClassLogs.equals(_existingClassLogs)) {
          return new RoomOpenHelper.ValidationResult(false, "class_logs(com.classpulse.models.ClassLog).\n"
                  + " Expected:\n" + _infoClassLogs + "\n"
                  + " Found:\n" + _existingClassLogs);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "8d2db0ee9fa0ca56122408f11a2b78b3", "8dbf3ecb339a86998cc24610e612af18");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "subjects","class_logs");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    final boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `subjects`");
      _db.execSQL("DELETE FROM `class_logs`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(SubjectDao.class, SubjectDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ClassLogDao.class, ClassLogDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public SubjectDao subjectDao() {
    if (_subjectDao != null) {
      return _subjectDao;
    } else {
      synchronized(this) {
        if(_subjectDao == null) {
          _subjectDao = new SubjectDao_Impl(this);
        }
        return _subjectDao;
      }
    }
  }

  @Override
  public ClassLogDao classLogDao() {
    if (_classLogDao != null) {
      return _classLogDao;
    } else {
      synchronized(this) {
        if(_classLogDao == null) {
          _classLogDao = new ClassLogDao_Impl(this);
        }
        return _classLogDao;
      }
    }
  }
}
