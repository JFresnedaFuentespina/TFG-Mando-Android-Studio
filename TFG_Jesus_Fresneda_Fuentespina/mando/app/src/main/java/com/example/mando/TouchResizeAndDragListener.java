package com.example.mando;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

/**
 *  Clase para la gestión del cambio de posición y de tamaño de los botones.
 */
public class TouchResizeAndDragListener implements View.OnTouchListener {
    private final ScaleGestureDetector scaleDetector;
    private final View view;
    private float dX, dY;
    private boolean isScaling = false;

    public TouchResizeAndDragListener(Context context, View view) {
        this.view = view;
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            float scaleFactor = 1f;

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                isScaling = true;
                scaleFactor *= detector.getScaleFactor();
                scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f)); // Nueva escala mínima y máxima
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                isScaling = false;
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        scaleDetector.onTouchEvent(event);

        if (!isScaling) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    // Al inicio del toque, calculamos la diferencia entre la posición del toque y la vista
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    // Mover el botón con el toque
                    v.animate().x(event.getRawX() + dX).y(event.getRawY() + dY).setDuration(0).start();

                    // Actualizar la posición en los LayoutParams
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) v.getLayoutParams();
                    params.leftMargin = (int) (event.getRawX() + dX);
                    params.topMargin = (int) (event.getRawY() + dY);
                    v.setLayoutParams(params); // Aplicar los nuevos parámetros

                    break;

                case MotionEvent.ACTION_UP:
                    // Al final del movimiento, guardar la nueva posición en buttonAX y buttonAY
                    FrameLayout.LayoutParams finalParams = (FrameLayout.LayoutParams) v.getLayoutParams();
                    float buttonAX = finalParams.leftMargin;
                    float buttonAY = finalParams.topMargin;
                    break;
            }
        }

        return true;
    }

}

