package com.mrkiriss.wifilocalpositioning.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

public class CustomPhotoView extends PhotoView {
    public CustomPhotoView(Context context) {
        super(context);
    }
    public CustomPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }
    public CustomPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                performClick();
                return true;
        }
        return false;
    }


    @Override
    public boolean performClick() {
        super.performClick();
        doSomething();
        return true;
    }

    private void doSomething() {
        Toast.makeText(getContext(), "did something", Toast.LENGTH_SHORT).show();
    }
}
