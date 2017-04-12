package ru.urfu.colorpicker.color_picker;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import ru.urfu.colorpicker.utils.SizeManager;

public class CellColorView extends View implements View.OnTouchListener
{
    private static LinearLayout.LayoutParams defaultParams;
    private static LinearLayout.LayoutParams scaledParams;
    public static int size = SizeManager.dpToPx(65);

    static {
        int margin = (int) (size * 0.25);
        defaultParams = new LinearLayout.LayoutParams(size, size);
        defaultParams.setMargins(margin, margin, margin, margin);
        scaledParams = new LinearLayout.LayoutParams(size + size/2, size + size/2);
    }

    private GestureDetector gestureDetector;
    private PickerView parent;
    private int color;

    private CellColorView(Context context) {
        super(context);
    }

    private CellColorView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private CellColorView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CellColorView setCellColor(int color)
    {
        this.color = color;
        setBackgroundColor(color);
        return this;
    }

    private CellColorView setDefaultLayoutParams(ViewGroup.LayoutParams params)
    {
        setLayoutParams(params);
        return this;
    }

    private CellColorView build(AbstractPickerView view) {
        this.parent = (PickerView) view;
        this.setOnTouchListener(this);

        gestureDetector = new GestureDetector(getContext(), new GestureListener());

        return this;
    }

    public static CellColorView create(AbstractPickerView pickerView)
    {
        return new CellColorView(pickerView.getContext())
                .setDefaultLayoutParams(defaultParams)
                .build(pickerView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestureDetector.onTouchEvent(e);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (parent.isEnableEditMode())
                    offsetColor(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                parent.notifySubscribers(Action.editModeDisable);
                this.setLayoutParams(defaultParams);
                this.invalidate();
                break;
        }

        return false;
    }


    private void offsetColor(float x, float y) {
        float from = (float) (size / 2);
        int distanceX = (int) (x - from);
        int distanceY = (int) (y - from);
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] = hsv[0] + (distanceX / 10);
        hsv[2] = hsv[2] - (distanceY * 0.001f);
        setBackgroundColor(Color.HSVToColor(hsv));
    }


    private class GestureListener extends GestureDetector.SimpleOnGestureListener
    {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            parent.setCurrentColor(((ColorDrawable) getBackground()).getColor());
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            parent.notifySubscribers(Action.editModeEnable);
            CellColorView.this.setLayoutParams(scaledParams);
            CellColorView.this.invalidate();
            super.onLongPress(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            setBackgroundColor(color);
            return true;
        }
    }
}
