package ru.urfu.taskmanager.data.db.async;

import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.List;

import ru.urfu.taskmanager.data.db.DbFilter;


@SuppressWarnings("unused")
public interface AsyncExecutor<T>
{
    void getAllEntries(@NonNull ExecuteController<List<T>> controller);

    void getEntryById(int id, ExecuteController<T> controller);

    void insertEntry(@NonNull T entry);

    void insertEntry(@NonNull T entry, @NonNull ExecuteController<T> controller);

    void removeEntryById(int id);

    void removeEntryById(int id, @NonNull ExecuteController<T> controller);

    void updateEntry(@NonNull T entry);

    void updateEntry(@NonNull T entry, @NonNull ExecuteController<T> controller);

    void replaceAll(@NonNull List<T> entries, @NonNull ExecuteController<Void> controller);

    void getCursor(@NonNull DbFilter filter, @NonNull ExecuteController<Cursor> controller);

    void startTransaction(ExecuteController<Void> controller);
}
