package ru.urfu.taskmanager.task_manager.main.fragments.view;

import android.database.Cursor;

import ru.urfu.taskmanager.task_manager.main.adapters.OnDataUpdateListener;
import ru.urfu.taskmanager.utils.interfaces.Showable;

public interface TaskListView extends OnDataUpdateListener<Cursor>, Showable
{
    int getDataType();

}
