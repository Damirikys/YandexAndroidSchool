package ru.urfu.taskmanager.task_manager.main.presenter;

import android.content.Intent;
import android.net.Uri;

import com.squareup.moshi.Types;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.urfu.taskmanager.R;
import ru.urfu.taskmanager.task_manager.fragments.view.TaskListView;
import ru.urfu.taskmanager.task_manager.main.view.TaskManager;
import ru.urfu.taskmanager.task_manager.models.TaskEntry;
import ru.urfu.taskmanager.utils.db.TasksDatabase;
import ru.urfu.taskmanager.utils.db.TasksFilter;
import ru.urfu.taskmanager.utils.interfaces.Callback;
import ru.urfu.taskmanager.utils.interfaces.Coupler;
import ru.urfu.taskmanager.utils.tools.JSONFactory;

import static android.app.Activity.RESULT_OK;
import static ru.urfu.taskmanager.task_manager.main.view.TaskManagerActivity.REQUEST_CREATE;
import static ru.urfu.taskmanager.task_manager.main.view.TaskManagerActivity.REQUEST_EDIT;
import static ru.urfu.taskmanager.task_manager.main.view.TaskManagerActivity.REQUEST_IMPORT;

public class TaskManagerPresenterImpl implements TaskManagerPresenter
{
    private final TaskManager mManager;
    private final TasksDatabase mDatabase;
    private final List<TaskListView> mTasksList;

    public TaskManagerPresenterImpl(TaskManager view) {
        this.mManager = view;
        this.mTasksList = new ArrayList<>();
        this.mDatabase = TasksDatabase.getInstance();
    }

    @Override
    public void taskIsCompleted(int id) {
        mDatabase.updateEntry(
                mDatabase.getEntryById(id)
                        .setTtl(System.currentTimeMillis())
                        .setCompleted(true)
        );

        notifyDataUpdate();
    }

    @Override
    public void postponeTheTask(int id, Coupler<Callback<Date>, TaskEntry> coupler) {
        TaskEntry task = mDatabase.getEntryById(id);
        coupler.bind(date -> {
            task.setTtl(date.getTime());
            mDatabase.updateEntry(task);
            notifyDataUpdate();
        }, task);
    }

    @Override
    public void deleteTheTask(int id) {
        mDatabase.removeEntryById(id);
        notifyDataUpdate();
    }

    @Override
    public void restoreTheTask(int id, Coupler<Callback<Date>, TaskEntry> coupler) {
        TaskEntry task = mDatabase.getEntryById(id)
                .setCompleted(false);

        coupler.bind(date -> {
            task.setTtl(date.getTime());
            mDatabase.updateEntry(task);
            notifyDataUpdate();
        }, task);
    }

    @Override
    public void editTheTask(int id) {
        mManager.startEditor(id);
    }

    @Override
    public TaskListView bindView(TaskListView view) {
        mTasksList.add(view);
        return view.bindPresenter(this);
    }

    @Override
    public void applyFilter(TasksFilter.Builder filter) {
        notifyDataUpdate(filter);
    }

    @Override
    public void onResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CREATE:
                    mManager.showAlert(mManager.getResources().getString(R.string.task_was_created));
                    break;
                case REQUEST_EDIT:
                    mManager.showAlert(mManager.getResources().getString(R.string.task_was_updated));
                    break;
                case REQUEST_IMPORT:
                    importFrom(data.getData());
                    break;
            }

            notifyDataUpdate();
        }
    }

    private void importFrom(Uri uri) {
        StringBuilder builder = new StringBuilder();
        try {
            InputStream inputStream = mManager.getBaseContext()
                    .getContentResolver()
                    .openInputStream(uri);

            if (inputStream == null)
                throw new FileNotFoundException();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream)
            );

            String line;
            while ((line = br.readLine()) != null) {
                builder.append(line);
                builder.append('\n');
            }

            br.close();

            List<TaskEntry> entries = JSONFactory.fromJson(builder.toString(),
                    Types.newParameterizedType(List.class, TaskEntry.class));

            mDatabase.replaceAll(entries);
            mManager.showAlert(mManager.getResources().getString(R.string.task_successful_import));
        } catch (IOException e) {
            e.printStackTrace();
            mManager.showAlert(mManager.getResources().getString(R.string.task_import_failed));
        }
    }

    private void notifyDataUpdate() {
        notifyDataUpdate(TasksFilter.DEFAULT_BUILDER);
    }

    private void notifyDataUpdate(TasksFilter.Builder builder) {
        for (TaskListView taskList : mTasksList) {
            taskList.onUpdate(builder.copy());
        }
    }
}
