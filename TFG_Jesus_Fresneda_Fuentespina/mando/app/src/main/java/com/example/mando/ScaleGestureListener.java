package com.example.mando;

import android.view.ScaleGestureDetector;
import android.view.View;

/**
 *  Clase para la gestión del cambio de escalado de los botones.
 */
public class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    private View view;
    private float scaleFactor = 1.0f;

    public ScaleGestureListener(View view) {
        this.view = view;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(0.5f, Math.min(scaleFactor, 2.0f)); // Limita escala
        view.setScaleX(scaleFactor);
        view.setScaleY(scaleFactor);
        return true;
    }
}
