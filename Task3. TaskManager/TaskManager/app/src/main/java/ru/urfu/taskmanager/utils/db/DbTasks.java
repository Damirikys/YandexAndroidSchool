package ru.urfu.taskmanager.utils.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import ru.urfu.taskmanager.task_manager.models.TaskEntry;
import ru.urfu.taskmanager.utils.db.async.DbAsyncExecutor;
import ru.urfu.taskmanager.utils.interfaces.Callback;

import static ru.urfu.taskmanager.utils.db.DbTasksFilter.COMPLETED_TASK;

public class DbTasks implements SimpleDatabase<TaskEntry>
{
    private static DbTasks sInstance;
    private SQLiteDatabase mDatabase;
    private DbAsyncExecutor<TaskEntry> mAsyncExecutor;

    private DbTasks(Context c) {
        super();
        DbTasksHelper dbHelper = new DbTasksHelper(c);
        this.mDatabase = dbHelper.getWritableDatabase();
        this.mAsyncExecutor = new DbAsyncExecutor<>(this);
    }

    @Override
    public List<TaskEntry> getAllEntries() {
        List<TaskEntry> entries = new ArrayList<>();
        Cursor cursor = getCursor(DbTasksFilter.DEFAULT_BUILDER.build());
        cursor.moveToFirst();

        do {
            entries.add(getCurrentEntryFromCursor(cursor));
        } while (cursor.moveToNext());

        return entries;
    }

    @Override
    public void insertEntry(TaskEntry entry) {
        mDatabase.insert(DbTasksHelper.TABLE_NAME, null, contentValuesFrom(entry));
    }

    @Override
    public void removeEntryById(int id) {
        mDatabase.delete(DbTasksHelper.TABLE_NAME, DbTasksHelper.ID + " = " + id, null);
    }

    @Override
    public TaskEntry updateEntry(TaskEntry entry) {
        mDatabase.update(DbTasksHelper.TABLE_NAME, contentValuesFrom(entry), DbTasksHelper.ID + " = " + entry.getId(), null);
        return getEntryById(entry.getId());
    }

    @Override
    public void startTransaction(Callback<Void> callback) {
        mDatabase.execSQL("PRAGMA synchronous=OFF");
        mDatabase.beginTransaction();
        try {
            callback.call(null);
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDatabase.endTransaction();
            mDatabase.execSQL("PRAGMA synchronous=ON");
        }
    }

    @Override
    public TaskEntry getEntryById(int id) {
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " +
                DbTasksHelper.TABLE_NAME + " WHERE " + DbTasksHelper.ID + "=" + id, null);

        if (cursor.getCount() == 0) return null;

        cursor.moveToFirst();
        return getCurrentEntryFromCursor(cursor);
    }

    @Override
    public void replaceAll(List<TaskEntry> entries) {
        mDatabase.delete(DbTasksHelper.TABLE_NAME, null, null);
        startTransaction(obj -> {
            for (TaskEntry entry : entries)
                insertEntry(entry);
        });
    }


    public TaskEntry getCurrentEntryFromCursor(Cursor cursor) {
        int id = cursor.getColumnIndex(DbTasksHelper.ID);
        int title = cursor.getColumnIndex(DbTasksHelper.TITLE);
        int timetolive = cursor.getColumnIndex(DbTasksHelper.TTL);
        int time_edited = cursor.getColumnIndex(DbTasksHelper.TIME_EDITED);
        int time_created = cursor.getColumnIndex(DbTasksHelper.TIME_CREATED);
        int description = cursor.getColumnIndex(DbTasksHelper.DESCRIPTION);
        int isCompleted = cursor.getColumnIndex(DbTasksHelper.COMPLETED);
        int decorate_color = cursor.getColumnIndex(DbTasksHelper.DECORATE_COLOR);
        int image_url = cursor.getColumnIndex(DbTasksHelper.IMAGE_URL);

        return new TaskEntry(cursor.getInt(id))
                .setTitle(cursor.getString(title))
                .setDescription(cursor.getString(description))
                .setTtl(Long.valueOf(cursor.getString(timetolive)))
                .setCreated(Long.valueOf(cursor.getString(time_created)))
                .setEdited(Long.valueOf(cursor.getString(time_edited)))
                .setTtl(Long.valueOf(cursor.getString(timetolive)))
                .setColor(cursor.getInt(decorate_color))
                .setImageUrl(cursor.getString(image_url))
                .setCompleted(cursor.getInt(isCompleted) == COMPLETED_TASK);
    }

    public Cursor getCursor(DbFilter filter) {
        return mDatabase.query(DbTasksHelper.TABLE_NAME,
                filter.getColumns(),
                filter.getWhereClause(),
                filter.getSelectionArgs(),
                filter.getGroupBy(),
                filter.getHaving(),
                filter.getOrderBy()
        );
    }

    public DbAsyncExecutor<TaskEntry> getAsyncExecutor() {
        return mAsyncExecutor;
    }


    private ContentValues contentValuesFrom(TaskEntry entry) {
        ContentValues values = new ContentValues();
        if (entry.getTtl() != null)
            values.put(DbTasksHelper.TTL, entry.getTtlTimestamp());
        if (entry.getTitle() != null)
            values.put(DbTasksHelper.TITLE, entry.getTitle());
        if (entry.getDescription() != null)
            values.put(DbTasksHelper.DESCRIPTION, entry.getDescription());
        if (entry.getEdited() != null)
            values.put(DbTasksHelper.TIME_EDITED, entry.getEditedTimestamp());
        if (entry.getCreated() != null)
            values.put(DbTasksHelper.TIME_CREATED, entry.getCreatedTimestamp());
        if (entry.getColor() != null)
            values.put(DbTasksHelper.DECORATE_COLOR, entry.getColorInt());
        if (entry.getImageUrl() != null)
            values.put(DbTasksHelper.IMAGE_URL, entry.getImageUrl());

        values.put(DbTasksHelper.COMPLETED, entry.isCompleted());

        return values;
    }


    public static DbTasks getInstance() {
        return sInstance;
    }

    public static void init(Context applicationContext) {
        sInstance = new DbTasks(applicationContext);
    }
}
