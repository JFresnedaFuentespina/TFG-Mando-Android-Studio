package com.example.mando;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

/**
 *  Crea un Joystick y calcula la velocidad a la que mueve el objeto que controlamos.
 */
public class JoystickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private float centerX, centerY, baseRadius, hatRadius;
    private Vector velocity;


    public JoystickView(Context context) {
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        velocity = new Vector(0, 0);
    }

    public JoystickView(Context context, AttributeSet attributes, int style) {
        super(context, attributes, style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        velocity = new Vector(0, 0);
    }

    public JoystickView(Context context, AttributeSet attributes) {
        super(context, attributes);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        velocity = new Vector();
    }

    /**
     *  Función para inicializar las dimensiones del Joystick.
     */
    private void setupDimensions() {
        centerX = getWidth() / 5f; // Ubicado en la parte izquierda
        centerY = getHeight() * 0.75f; // Ubicado en la parte inferior

        baseRadius = Math.min(getWidth(), getHeight()) / 6f; // Reducido (antes era /4)
        hatRadius = Math.min(getWidth(), getHeight()) / 10f; // Más pequeño (antes era /6)
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX, centerY);
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }

    /**
     * Función que gestiona lo que ocurre cuando el usuario utiliza el Joystick.
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return true
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float newX = event.getX();
            float newY = event.getY();

            // Calcular la distancia del punto actual al centro del joystick
            float deltaX = newX - centerX;
            float deltaY = newY - centerY;
            double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

            // Si la distancia es mayor que el radio permitido, limitamos la posición
            if (distance > baseRadius) {
                double ratio = baseRadius / distance; // Factor de reducción
                newX = centerX + (float) (deltaX * ratio);
                newY = centerY + (float) (deltaY * ratio);
            }

            // Normalizamos el valor y actualizamos el objeto velocidad
            double speedX = (newX - centerX) / baseRadius;
            double speedY = (newY - centerY) / baseRadius;

            velocity.setX(speedX * 10);
            velocity.setY(speedY * 10);

            drawJoystick(newX, newY);
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            // Resetear el joystick a la posición central cuando se levante el dedo
            drawJoystick(centerX, centerY);
            // Resetear la velocidad a cero cuando se suelta el joystick
            velocity.setX(0);
            velocity.setY(0);
        }
        return true;
    }

    /**
     * Función para guardar nuevas posiciones del centro del Joystick
     * @param x
     * @param y
     */
    public void setJoystickCenter(float x, float y) {
        this.centerX = x;
        this.centerY = y;
        drawJoystick(centerX, centerY);
    }

    public float getJoystickCenterX() {
        return centerX;
    }

    public float getJoystickCenterY() {
        return centerY;
    }

    /**
     *  Función para pintar en la pantalla el Joystick.
     * @param newX
     * @param newY
     */
    public void drawJoystick(float newX, float newY) {
        if (getHolder().getSurface().isValid()) {
            Canvas myCanvas = this.getHolder().lockCanvas();
            // Limpiar la superficie para dibujar desde cero
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

            // Cargar la imagen de fondo (esto puede ser tu fondo de espacio o lo que sea que estés usando)
            Bitmap backgroundBitmap = ((BitmapDrawable) getResources().getDrawable(R.drawable.space_background)).getBitmap();
            myCanvas.drawBitmap(backgroundBitmap, 0, 0, null); // Dibuja la imagen de fondo en toda la superficie

            // Dibuja el círculo grande (base del joystick)
            Paint colors = new Paint();
            colors.setARGB(255, 50, 50, 50);  // Color gris para la base
            myCanvas.drawCircle(centerX, centerY, baseRadius, colors);  // Fondo, sin el joystick

            // Dibuja el círculo pequeño (hat del joystick)
            colors.setARGB(255, 0, 0, 255);  // Color azul para el joystick
            myCanvas.drawCircle(newX, newY, hatRadius, colors);  // Posición actual del joystick

            // Liberar el canvas para actualizar la vista
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }

    public Vector getVelocity() {
        return this.velocity;
    }
}
