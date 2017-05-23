package ru.urfu.taskmanager.data.network;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import ru.urfu.taskmanager.task_manager.models.TaskEntry;
import ru.urfu.taskmanager.data.db.DbFilter;
import ru.urfu.taskmanager.data.db.DbTasks;
import ru.urfu.taskmanager.data.db.async.AsyncExecutor;
import ru.urfu.taskmanager.data.db.async.DbAsyncExecutor;
import ru.urfu.taskmanager.data.db.async.ExecuteController;
import ru.urfu.taskmanager.data.db.async.ExecuteControllerAdapter;
import ru.urfu.taskmanager.data.network.sync_module.BroadcastSyncManager;
import ru.urfu.taskmanager.utils.tools.NetworkUtil;

public class APIServiceExecutor implements AsyncExecutor<TaskEntry>
{
    private Context mContext;
    private DbAsyncExecutor<TaskEntry> mDbAsyncExecutor = DbTasks.getInstance().getAsyncExecutor();
    private APIService mApiService;

    private APICallback<Void> mFailedCallback = new APICallback<Void>() {
        @Override
        public void onFailure(Throwable t) {
            mContext.sendBroadcast(new Intent(BroadcastSyncManager.SYNC_SCHEDULE_ACTION));
        }
    };

    public APIServiceExecutor(Context context, APIService service) {
        this.mContext = context;
        this.mApiService = service;
    }

    private boolean isConnected() {
        boolean isConnected = NetworkUtil.networkIsReachable(mContext);
        if (!isConnected) {
            mFailedCallback.onFailure(new NetworkErrorException());
        }

        return isConnected;
    }

    @Override
    public void getAllEntries(@NonNull ExecuteController<List<TaskEntry>> controller) {
        if (isConnected()) {
            controller.onStart();
            mApiService.getUserNotes().send(new APICallback<Collection<TaskEntry>>()
            {
                @Override
                public void onResponse(APIResponse<Collection<TaskEntry>> response) {
                    controller.onFinish(new ArrayList<>(response.getBody()));
                }

                @Override
                public void onFailure(Throwable t) {
                    mFailedCallback.onFailure(t);
                }
            });
        } else {
            mDbAsyncExecutor.getAllEntries(controller);
        }
    }

    @Override
    public void getEntryById(int id, ExecuteController<TaskEntry> controller) {
        if (isConnected()) {
            controller.onStart();
            mApiService.getNoteById(id).send(new APICallback<TaskEntry>()
            {
                @Override
                public void onResponse(APIResponse<TaskEntry> response) {
                    controller.onFinish(response.getBody());
                }

                @Override
                public void onFailure(Throwable t) {
                    mFailedCallback.onFailure(t);
                }
            });
        } else {
            mDbAsyncExecutor.getEntryById(id, controller);
        }
    }

    @Override
    public void insertEntry(@NonNull TaskEntry entry) {
        if (isConnected()) {
            mApiService.createNote(entry).send(new APICallback<Integer>()
            {
                @Override
                public void onResponse(APIResponse<Integer> response) {
                    mDbAsyncExecutor.insertEntry(entry.setEntryId(response.getBody()));
                }

                @Override
                public void onFailure(Throwable t) {
                    mFailedCallback.onFailure(t);
                }
            });
        } else {
            mDbAsyncExecutor.insertEntry(entry.setEntryId(-1));
        }
    }

    @Override
    public void insertEntry(@NonNull TaskEntry entry, @NonNull ExecuteController<TaskEntry> controller) {
        mDbAsyncExecutor.insertEntry(entry, new ExecuteControllerAdapter<TaskEntry>()
        {
            @Override
            public void onFinish(TaskEntry result) {
                controller.onFinish(result);

                if (isConnected()) {
                    mApiService.createNote(result).send(new APICallback<Integer>()
                    {
                        @Override
                        public void onResponse(APIResponse<Integer> response) {
                            TaskEntry toUpdate = result.setEntryId(response.getBody());
                            mDbAsyncExecutor.updateEntry(toUpdate);
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            mFailedCallback.onFailure(t);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void removeEntryById(int id) {
        if (isConnected()) {
            mApiService.deleteNote(id).send(mFailedCallback);
        } else {
            mDbAsyncExecutor.removeEntryById(id);
        }
    }

    @Override
    public void removeEntryById(int id, @NonNull ExecuteController<TaskEntry> controller) {
        controller.onStart();
        mDbAsyncExecutor.removeEntryById(id, new ExecuteControllerAdapter<TaskEntry>()
        {
            @Override
            public void onFinish(TaskEntry deleted) {
                if (isConnected()) {
                    mApiService.deleteNote(deleted.getEntryId()).send(mFailedCallback);
                }

                controller.onFinish(deleted);
            }
        });
    }

    @Override
    public void updateEntry(@NonNull TaskEntry entry) {
        mDbAsyncExecutor.updateEntry(entry, new ExecuteControllerAdapter<TaskEntry>()
        {
            @Override
            public void onFinish(TaskEntry result) {
                if (isConnected()) mApiService.editNote(result).send(mFailedCallback);
            }
        });
    }

    @Override
    public void updateEntry(@NonNull TaskEntry entry, @NonNull ExecuteController<TaskEntry> controller) {
        controller.onStart();
        mDbAsyncExecutor.updateEntry(entry, new ExecuteControllerAdapter<TaskEntry>()
        {
            @Override
            public void onFinish(TaskEntry result) {
                controller.onFinish(result);
                if (isConnected()) mApiService.editNote(result).send(mFailedCallback);
            }
        });
    }

    @Override
    public void replaceAll(@NonNull List<TaskEntry> entries, @NonNull ExecuteController<Void> controller) {
        mDbAsyncExecutor.replaceAll(entries, controller);
    }

    @Override
    public void getCursor(@NonNull DbFilter filter, @NonNull ExecuteController<Cursor> controller) {
        mDbAsyncExecutor.getCursor(filter, controller);
    }

    @Override
    public void startTransaction(ExecuteController<Void> controller) {
        mDbAsyncExecutor.startTransaction(controller);
    }
}