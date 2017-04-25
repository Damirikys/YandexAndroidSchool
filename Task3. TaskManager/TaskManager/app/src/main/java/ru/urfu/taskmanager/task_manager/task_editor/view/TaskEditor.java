package ru.urfu.taskmanager.task_manager.task_editor.view;

import android.content.Intent;
import android.view.View;

import ru.urfu.taskmanager.color_picker.listeners.PickerViewStateListener;
import ru.urfu.taskmanager.task_manager.models.TaskEntry;

public interface TaskEditor extends View.OnClickListener, PickerViewStateListener {
    Intent getIntent();
    boolean isRestored();
    void setToolbarTitle(String title);
    void initializeEditor(TaskEntry entryToEdit);
    void showTitleError(String string);
    void showDescriptionError(String string);
    void exit(int result);
}
