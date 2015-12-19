package com.bcgs.bruno.drawing;

import android.graphics.Matrix;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

/**
 *
 * Created by Igor on 12/16/2015.
 */

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

    private float scale = 1f;

    private CanvasView cv;

    public ScaleListener(CanvasView cv) {
        this.cv = cv;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scale *= detector.getScaleFactor();
        scale = Math.max(0.1f, Math.min(scale, 5.0f));

        this.cv.setScaleX(scale);
        this.cv.setScaleY(scale);


        Log.d("Detectou gesto", "detectou touch");

        return true;
    }
}

