package ru.urfu.taskmanager.task_manager.task_editor.view;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.github.florent37.singledateandtimepicker.SingleDateAndTimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ru.urfu.taskmanager.R;
import ru.urfu.taskmanager.color_picker.PickerView;
import ru.urfu.taskmanager.task_manager.task_editor.presenter.TaskEditorPresenter;
import ru.urfu.taskmanager.task_manager.task_editor.presenter.TaskEditorPresenterImpl;
import ru.urfu.taskmanager.task_manager.models.TaskEntry;

public class TaskEditorActivity extends AppCompatActivity implements TaskEditor
{
    private static final String CACHE_KEY = "color_cache";
    private static final String COLOR_KEY = "current_color";
    private static final String DATE_KEY = "selected_date";
    private static final int CELL_COUNT = 16;

    TaskEditorPresenter presenter;

    SingleDateAndTimePicker dateAndTimePicker;
    PickerView pickerView;
    CardView cardColorView;
    EditText title_edit_field, desc_edit_field;
    Button save_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_editor);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.editor_create_title));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setResult(RESULT_CANCELED);

        initView();
    }

    private void initView() {
        dateAndTimePicker = (SingleDateAndTimePicker) findViewById(R.id.datetime_picker);
        dateAndTimePicker.setMustBeOnFuture(true);
        pickerView = (PickerView) findViewById(R.id.pickerView);
        cardColorView = (CardView) findViewById(R.id.cardColor);
        title_edit_field = (EditText) findViewById(R.id.title_edit_field);
        desc_edit_field = (EditText) findViewById(R.id.descr_edit_field);
        save_button = (Button) findViewById(R.id.save_button);
        save_button.setOnClickListener(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        cardColorView.setCardBackgroundColor(pickerView.getCurrentColor());
        pickerView.subscribe(this);
        pickerView.setCellCount(CELL_COUNT);

        presenter = new TaskEditorPresenterImpl(this);
    }

    public void initializeEditor(TaskEntry entry) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(entry.getTtl());

        dateAndTimePicker.selectDate(calendar);
        dateAndTimePicker.setSelectorColor(Color.BLACK);
        title_edit_field.setText(entry.getTitle());
        desc_edit_field.setText(entry.getDescription());
        pickerView.setCurrentColor(entry.getColor());
        cardColorView.setCardBackgroundColor(pickerView.getCurrentColor());
    }

    @Override
    public void onClick(View v) {
        setResult(RESULT_OK);
        presenter.saveState(
                new TaskEntry()
                    .setTitle(title_edit_field.getText().toString())
                    .setDescription(desc_edit_field.getText().toString())
                    .setTtl(dateAndTimePicker.getDate().getTime())
                    .setColor(pickerView.getCurrentColor())
        );

        finish();
    }

    @Override
    public void onColorChanged(int color) {
        cardColorView.setCardBackgroundColor(color);
    }

    @Override
    public void editModeEnable() {
        vibrate();
    }

    @Override
    public void editModeDisable() {
        vibrate();
    }

    @Override
    public void theBoundaryIsReached() {
        vibrate();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(10);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        pickerView.getColorCache();
        outState.putSerializable(CACHE_KEY, pickerView.getColorCache());
        outState.putInt(COLOR_KEY, pickerView.getCurrentColor());
        outState.putSerializable(DATE_KEY, dateAndTimePicker.getDate());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime((Date) savedInstanceState.getSerializable(DATE_KEY));
        float[][] cache = (float[][]) savedInstanceState.getSerializable(CACHE_KEY);
        int currentColor = savedInstanceState.getInt(COLOR_KEY);
        pickerView.setColorCache(cache);
        pickerView.setCellCount(CELL_COUNT);
        pickerView.setCurrentColor(currentColor);
        cardColorView.setCardBackgroundColor(pickerView.getCurrentColor());
        dateAndTimePicker.selectDate(calendar);
    }

    @Override
    public void setToolbarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
}