package com.example.mando;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // DECLARACIÓN DE VARIABLES
    // Envío de objetos
    private ObjectOutputStream oos;
    private Socket socket;
    private boolean isConnected;
    private CommsController server;
    // Joystick
    private boolean isJoystickView = true;
    private float vida;
    private ProgressBar barraVida;
    private boolean isCarView = false;
    private JoystickView joystickView;
    // Reproductor de sonido
    private MediaPlayer mediaPlayer;
    private SoundPool soundPool;

    //ImageButtons
    private ImageButton settings, playJoystick, playCar, editJoystick, editCar, acelerador, freno, volante, marchaAtras;

    //Posiciones de los botones del Joystick
    private int buttonAX = 1700;
    private int buttonAY = 550;
    private int joystickX = 350;
    private int joystickY = 700;

    // Posiciones y escala de los botones del mando del coche
    private float scaleAcelerador = 1.0f;
    private float scaleFreno = 1.0f;
    private float scaleVolante = 1.0f;
    private float scaleMarchaAtras = 1.0f;


    private int aceleradorX = 0, aceleradorY = 0;
    private int frenoX = 144, frenoY = 0;
    private int volanteX = 16, volanteY = 0;
    private int marchaAtrasX = 100, marchaAtrasY = 0;

    private boolean carLayoutInitialized = false;
    private boolean controllerInitialized = false;
    private boolean isMarchaAtras = false;
    private TextView textoCuentaAtras;
    private CountDownTimer countDownTimer;
    // Atributos OnTouch layout carreras
    private float velocidad = 0f;
    private final Handler handler = new Handler();
    private final Runnable[] aceleradorRunnable = new Runnable[1];
    private final Runnable[] frenoRunnable = new Runnable[1];
    private float ultimoAngulo = Float.NaN;
    private int vidaMaxima;


    @SuppressLint({"UseCompatLoadingForDrawables"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isConnected = false;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initRemoteController();
    }

    /**
     * Función que inicializa el mando
     */
    private void initRemoteController() {
        crearLayoutJoystick();
        // Inicializar SoundPool
        soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        int motorSoundId = soundPool.load(this, R.raw.motor_sound, 1);
        settings = findViewById(R.id.btnSettings);
        if (settings != null) {
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cambiarLayout();
                }
            });
        }
    }

    /**
     * Función que cambia entre un layout u otro
     */
    public void cambiarLayout() {
        setContentView(R.layout.settings_layout);
        playJoystick = findViewById(R.id.playJoystickLayout);
        playCar = findViewById(R.id.playJCarLayout);
        editJoystick = findViewById(R.id.editJoystickLayout);
        editCar = findViewById(R.id.editCarLayout);
        LayoutInflater inflater = LayoutInflater.from(this);
        playCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.car_layout);
                if (!carLayoutInitialized) {
                    initCarLayout();
                } else {
                    loadCarLayout();
                }
            }
        });

        playJoystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);
                crearLayoutJoystick();
            }
        });

        editCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.edit_layout);
                mostrarVistaEnEditLayout(R.layout.car_layout);
                crearVistaEditCar();
            }
        });

        editJoystick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.edit_layout);
                FrameLayout container = findViewById(R.id.layoutPreviewContainer);
                container.removeAllViews();
                container.addView(crearJoystickViewSoloVisual());
            }
        });
    }

    /**
     * Función que muestra el layout indicado por parámetro para su edición
     *
     * @param layoutId
     */
    private void mostrarVistaEnEditLayout(int layoutId) {
        FrameLayout layoutContainer = findViewById(R.id.layoutPreviewContainer);
        LayoutInflater inflater = LayoutInflater.from(this);
        layoutContainer.removeAllViews();
        View layout = inflater.inflate(layoutId, layoutContainer, false);
        layoutContainer.addView(layout);
    }

    /**
     * Función que muestra los ImageButtons de los botones del layout del juego de carreras
     * para su edición. Finalmente guarda los nuevos valores y los aplica.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void crearVistaEditCar() {
        FrameLayout layoutContainer = findViewById(R.id.layoutPreviewContainer);
        layoutContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        View carView = inflater.inflate(R.layout.car_layout, layoutContainer, false);
        layoutContainer.addView(carView);

        ImageButton acelerador = carView.findViewById(R.id.btnAcelerar);
        ImageButton freno = carView.findViewById(R.id.btnFrenar);
        ImageButton volante = carView.findViewById(R.id.volante);
        ImageButton marchaAtras = carView.findViewById(R.id.marchaAtras);


        // === Aplicar escala y posición al Acelerador ===
        int width = 200;
        int height = 200;
        FrameLayout.LayoutParams aceleradorParams = new FrameLayout.LayoutParams(width, height);
        aceleradorParams.leftMargin = aceleradorX;
        aceleradorParams.topMargin = aceleradorY;
        acelerador.setLayoutParams(aceleradorParams);
        acelerador.setScaleX(scaleAcelerador);
        acelerador.setScaleY(scaleAcelerador);

        // === Aplicar escala y posición al Freno ===
        FrameLayout.LayoutParams frenoParams = new FrameLayout.LayoutParams(width, height);
        frenoParams.leftMargin = frenoX;
        frenoParams.topMargin = frenoY;
        freno.setLayoutParams(frenoParams);
        freno.setScaleX(scaleFreno);
        freno.setScaleY(scaleFreno);

        // === Aplicar escala y posición al Volante ===
        FrameLayout.LayoutParams volanteParams = new FrameLayout.LayoutParams(width, height);
        volanteParams.leftMargin = volanteX;
        volanteParams.topMargin = volanteY;
        volante.setLayoutParams(volanteParams);
        volante.setScaleX(scaleVolante);
        volante.setScaleY(scaleVolante);

        // === Aplicar escala y posición al cambio de marchas ===
        FrameLayout.LayoutParams marchaParams = new FrameLayout.LayoutParams(width, height);
        marchaParams.leftMargin = marchaAtrasX;
        marchaParams.topMargin = marchaAtrasY;
        volante.setLayoutParams(volanteParams);
        volante.setScaleX(scaleMarchaAtras);
        volante.setScaleY(scaleMarchaAtras);

        // Habilitar movimiento y redimensión
        acelerador.setOnTouchListener(new TouchResizeAndDragListener(this, acelerador));
        freno.setOnTouchListener(new TouchResizeAndDragListener(this, freno));
        volante.setOnTouchListener(new TouchResizeAndDragListener(this, volante));
        marchaAtras.setOnTouchListener(new TouchResizeAndDragListener(this, marchaAtras));

        // Botón guardar
        ImageButton guardarBtn = findViewById(R.id.btnSettings);
        ViewGroup parent = (ViewGroup) guardarBtn.getParent();
        if (parent != null) {
            parent.removeView(guardarBtn);
        }

        FrameLayout.LayoutParams guardarParams = new FrameLayout.LayoutParams(150, 150);
        guardarParams.gravity = Gravity.TOP | Gravity.END;
        guardarParams.setMargins(0, 50, 50, 0);
        guardarBtn.setLayoutParams(guardarParams);
        guardarBtn.setBackground(null);
        guardarBtn.setImageResource(R.drawable.guardar);

        guardarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aceleradorX = (int) acelerador.getX();
                aceleradorY = (int) acelerador.getY();
                scaleAcelerador = acelerador.getScaleX();

                frenoX = (int) freno.getX();
                frenoY = (int) freno.getY();
                scaleFreno = freno.getScaleX();

                volanteX = (int) volante.getX();
                volanteY = (int) volante.getY();
                scaleVolante = volante.getScaleX();

                marchaAtrasX = (int) marchaAtras.getX();
                marchaAtrasY = (int) marchaAtras.getY();
                scaleMarchaAtras = marchaAtras.getScaleX();

                Log.d("POS ACC: ", aceleradorX + ", " + aceleradorY + ", Scale: " + scaleAcelerador);
                Log.d("POS BRK: ", frenoX + ", " + frenoY + ", Scale: " + scaleFreno);
                Log.d("POS VOLANTE: ", volanteX + ", " + volanteY + ", Scale: " + scaleVolante);

                setContentView(R.layout.car_layout);
                loadCarLayout();
            }
        });

        layoutContainer.addView(guardarBtn);
    }

    /**
     * Función que inicializa el layout del juego de carreras.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initCarLayout() {
        textoCuentaAtras = findViewById(R.id.textoCuentaAtras);
        isJoystickView = false;
        isCarView = true;
        // Configurar el botón de Settings
        ImageButton settings = findViewById(R.id.btnSettings);
        settings.setOnClickListener(v -> cambiarLayout());

        // Recuperar los botones de car_layout
        acelerador = findViewById(R.id.btnAcelerar);
        freno = findViewById(R.id.btnFrenar);
        volante = findViewById(R.id.volante);
        marchaAtras = findViewById(R.id.marchaAtras);

        // Aplicar setButtonTouchListener a cada uno de los botones
        if (acelerador != null) {
            setButtonTouchListener(acelerador);
        }
        if (freno != null) {
            setButtonTouchListener(freno);
        }
        if (volante != null) {
            setButtonTouchListener(volante);
        }
        if (marchaAtras != null) {
            marchaAtras.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMarchaAtras) {
                        isMarchaAtras = false;
                    } else {
                        isMarchaAtras = true;
                    }
                }
            });
        }
        carLayoutInitialized = true;
    }


    /**
     * Función que carga las posiciones y los tamaños de los ImageButtons de la plantilla de
     * carreras.
     */
    @SuppressLint("ClickableViewAccessibility")
    private void loadCarLayout() {
        // Configurar el botón de Settings
        ImageButton settings = findViewById(R.id.btnSettings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarLayout();  // Cambiar de layout (si es necesario)
            }
        });

        // Recuperar los botones de car_layout
        ImageButton acelerador = findViewById(R.id.btnAcelerar);
        ImageButton freno = findViewById(R.id.btnFrenar);
        ImageButton volante = findViewById(R.id.volante);
        ImageButton marchaAtras = findViewById(R.id.marchaAtras);

        // Restaurar sus posiciones y tamaños manualmente
        if (acelerador != null) {
            int widthAcelerador = 200;
            int heightAcelerador = 200;
            FrameLayout.LayoutParams aceleradorParams = new FrameLayout.LayoutParams(widthAcelerador, heightAcelerador);
            aceleradorParams.leftMargin = aceleradorX;
            aceleradorParams.topMargin = aceleradorY;
            acelerador.setLayoutParams(aceleradorParams);

            // Escala visual aplicada por separado
            acelerador.setScaleX(scaleAcelerador);
            acelerador.setScaleY(scaleAcelerador);
            setButtonTouchListener(acelerador);
        }

        if (freno != null) {
            int widthFreno = 200;
            int heightFreno = 200;
            FrameLayout.LayoutParams frenoParams = new FrameLayout.LayoutParams(widthFreno, heightFreno);
            frenoParams.leftMargin = frenoX;
            frenoParams.topMargin = frenoY;
            freno.setLayoutParams(frenoParams);

            // Escala visual aplicada por separado
            freno.setScaleX(scaleFreno);
            freno.setScaleY(scaleFreno);
            setButtonTouchListener(freno);
        }

        if (volante != null) {
            int width = 300;
            int height = 300;
            FrameLayout.LayoutParams volanteParams = new FrameLayout.LayoutParams(width, height);
            volanteParams.leftMargin = volanteX;
            volanteParams.topMargin = volanteY;
            volante.setLayoutParams(volanteParams);

            // Escala visual aplicada por separado
            volante.setScaleX(scaleVolante);
            volante.setScaleY(scaleVolante);
            setButtonTouchListener(volante);
        }

        if (marchaAtras != null) {
            int width = 300;
            int height = 300;
            FrameLayout.LayoutParams marchaAtrasParams = new FrameLayout.LayoutParams(width, height);
            marchaAtrasParams.leftMargin = marchaAtrasX;
            marchaAtrasParams.topMargin = marchaAtrasY;
            marchaAtras.setLayoutParams(marchaAtrasParams);

            // Escala visual aplicada por separado
            marchaAtras.setScaleX(scaleVolante);
            marchaAtras.setScaleY(scaleVolante);
            marchaAtras.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMarchaAtras) {
                        isMarchaAtras = false;
                    } else {
                        isMarchaAtras = true;
                    }
                }
            });
        }

        // Mostrar las posiciones y escalas en el log
        Log.d("PosicionBotones", "Acelerador - X: " + aceleradorX + ", Y: " + aceleradorY + ", Scale: " + scaleAcelerador);
        Log.d("PosicionBotones", "Freno - X: " + frenoX + ", Y: " + frenoY + ", Scale: " + scaleFreno);
        Log.d("PosicionBotones", "Volante - X: " + volanteX + ", Y: " + volanteY + ", Scale: " + scaleVolante);
    }


    /**
     * Función que añade los eventos necesarios cuando se pulsa un botón.
     *
     * @param button
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setButtonTouchListener(@NonNull ImageButton button) {
        button.setOnTouchListener((v, event) -> {
            int id = button.getId();
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (id == R.id.btnAcelerar) {
                        // Evitar que se ejecuten ambos a la vez si se está frenando
                        handler.removeCallbacks(frenoRunnable[0]); // Remover cualquier frenado pendiente

                        // Acelerar cuando el botón se presiona
                        aceleradorRunnable[0] = new Runnable() {
                            @Override
                            public void run() {
                                if (isMarchaAtras) {
                                    // Disminuir la velocidad si estamos en marcha atrás
                                    if (velocidad > -15) { // Límite inferior de velocidad
                                        velocidad -= 1f;
                                        if (isConnected) {
                                            sendMessageGson(new Message("velocidad_car", velocidad).toGson());
                                        }
                                        handler.postDelayed(this, 100); // Continuar acelerando (reduciendo) cada 100 ms
                                    }
                                } else {
                                    // Aumentar la velocidad si no estamos en marcha atrás
                                    if (velocidad < 15) { // Límite superior de velocidad
                                        velocidad += 1f;
                                        if (isConnected) {
                                            sendMessageGson(new Message("velocidad_car", velocidad).toGson());
                                        }
                                        handler.postDelayed(this, 100); // Continuar acelerando cada 100 ms
                                    }
                                }
                            }
                        };
                        handler.post(aceleradorRunnable[0]);
                    } else if (id == R.id.btnFrenar) {
                        // Evitar que se ejecute la aceleración si se está frenando
                        handler.removeCallbacks(aceleradorRunnable[0]); // Remover cualquier aceleración pendiente

                        // Frenar cuando se presiona el botón de frenar
                        frenoRunnable[0] = new Runnable() {
                            @Override
                            public void run() {
                                if (isMarchaAtras) {
                                    // Si estamos en marcha atrás, aumentar la velocidad hasta llegar a 0
                                    if (velocidad < -1) { // Solo aumentar la velocidad si es negativa
                                        velocidad += 1f;
                                        if (isConnected) {
                                            sendMessageGson(new Message("velocidad_car", velocidad).toGson());
                                        }
                                        handler.postDelayed(this, 100); // Continuar frenando (aumentando velocidad) cada 100 ms
                                    }
                                } else {
                                    // Si no estamos en marcha atrás, disminuir la velocidad hasta llegar a 0
                                    if (velocidad > 1) { // Solo reducir la velocidad si es positiva
                                        velocidad -= 1f;
                                        if (isConnected) {
                                            sendMessageGson(new Message("velocidad_car", velocidad).toGson());
                                        }
                                        handler.postDelayed(this, 100); // Continuar frenando (disminuyendo velocidad) cada 100 ms
                                    }
                                }
                            }
                        };
                        handler.post(frenoRunnable[0]);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (id == R.id.btnAcelerar) {
                        // Dejar de acelerar inmediatamente cuando se suelta el acelerador
                        handler.removeCallbacks(aceleradorRunnable[0]);

                        // Iniciar la desaceleración cuando se suelta el acelerador
                        frenoRunnable[0] = new Runnable() {
                            @Override
                            public void run() {
                                if (velocidad > 0) { // Solo reducir si la velocidad es positiva
                                    velocidad -= 0.5f; // Disminuir la velocidad gradualmente
                                    if (isConnected) {
                                        sendMessageGson(new Message("velocidad_car", velocidad).toGson());
                                    }
                                    handler.postDelayed(this, 100); // Continuar reduciendo la velocidad
                                }
                            }
                        };
                        handler.post(frenoRunnable[0]);
                    } else if (id == R.id.btnFrenar) {
                        // Dejar de frenar inmediatamente cuando se suelta el botón de freno
                        handler.removeCallbacks(frenoRunnable[0]);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    // Eliminar las tareas pendientes si se cancela el toque
                    handler.removeCallbacks(aceleradorRunnable[0]);
                    handler.removeCallbacks(frenoRunnable[0]);
                    break;
            }

            // Lógica del volante (esto no cambia)
            if (id == R.id.volante) {
                float centerX = v.getWidth() / 2f;
                float centerY = v.getHeight() / 2f;
                float x = event.getX();
                float y = event.getY();

                float dx = x - centerX;
                float dy = centerY - y;
                float nuevoAngulo = (float) Math.toDegrees(Math.atan2(dx, dy));

                float factorSuavizado = 0.2f;
                if (Float.isNaN(ultimoAngulo)) ultimoAngulo = nuevoAngulo;
                ultimoAngulo += factorSuavizado * (nuevoAngulo - ultimoAngulo);

                v.setRotation(ultimoAngulo);
                if (isConnected) {
                    sendMessageGson(new Message("angle_car", ultimoAngulo).toGson());
                }
            }

            return true;
        });
    }

    public void setLifeBar(float vida) {
        this.vida = vida;
        Log.d("NUEVA VIDA DE LA NAVE!!! ", vida + "");
        // Asegurar que la barra muestre bien el valor
        barraVida.setProgress((int) this.vida);

        // Cambiar color según el porcentaje
        int color;
        if (vida <= 10) {
            color = Color.RED;
        } else if (vida <= 50) {
            color = Color.YELLOW;
        } else {
            color = Color.GREEN;
        }

        // Cambiar dinámicamente el color del drawable
        LayerDrawable drawable = (LayerDrawable) barraVida.getProgressDrawable();
        Drawable progress = drawable.findDrawableByLayerId(android.R.id.progress);
        DrawableCompat.setTint(progress, color);

    }


    public void setVidaMaxima(int vida) {
        this.vidaMaxima = vida;
        setLifeBar(this.vidaMaxima);
    }

    /**
     * Función para crear el layout del Joystick.
     */
    private void crearLayoutJoystick() {
        isJoystickView = true;
        isCarView = false;
        mediaPlayer = MediaPlayer.create(this, R.raw.laser_gunshot);
        mediaPlayer.setLooping(false);

        barraVida = findViewById(R.id.lifeBar);
        setLifeBar(this.vida);

        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        //mainLayout.setBackground(getDrawable(R.drawable.space_background));

        // Asegúrate de que el joystick anterior se elimine
        if (joystickView != null) {
            mainLayout.removeView(joystickView);
        }

        // Crear el joystick nuevo
        joystickView = new JoystickView(this);
        FrameLayout.LayoutParams joystickParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        joystickParams.gravity = Gravity.CENTER;
        joystickView.setLayoutParams(joystickParams);

        mainLayout.addView(joystickView);

        ViewGroup parent = (ViewGroup) barraVida.getParent();
        if (parent != null) {
            parent.removeView(barraVida);
        }
        mainLayout.addView(barraVida);


        // Aplicar el centro del joystick
        joystickView.post(new Runnable() {
            @Override
            public void run() {
                if (joystickX != -1 && joystickY != -1) {
                    joystickView.setJoystickCenter(joystickX, joystickY);
                }
            }
        });


        // Botón A
        Button circularButton = new Button(this);
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(200, 200);
        buttonParams.leftMargin = buttonAX;
        buttonParams.topMargin = buttonAY;
        circularButton.setLayoutParams(buttonParams);
        circularButton.setBackground(getDrawable(R.drawable.action_button));
        circularButton.setText("A");
        circularButton.setTextSize(35);
        circularButton.setTextColor(Color.WHITE);
        circularButton.setGravity(Gravity.CENTER);

        circularButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Message msg = new Message("action", "dispara");
                if (isConnected) {
                    sendMessageGson(msg.toGson());
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.seekTo(0);
                } else {
                    mediaPlayer.start();
                }
            }
        });

        // Botón de configuración
        ImageButton settingsButton = new ImageButton(this);
        FrameLayout.LayoutParams settingsParams = new FrameLayout.LayoutParams(150, 150);
        settingsParams.gravity = Gravity.TOP | Gravity.END;
        settingsParams.setMargins(0, 50, 50, 0);
        settingsButton.setLayoutParams(settingsParams);
        settingsButton.setBackground(null);
        settingsButton.setPadding(5, 5, 5, 5);
        settingsButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        settingsButton.setImageResource(R.drawable.configuraciones);

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiarLayout();
            }
        });

        // Añadir elementos al layout principal
        mainLayout.addView(circularButton);
        mainLayout.addView(settingsButton);

        setContentView(mainLayout);
        startConnectionP2P();
    }

    private void startConnectionP2P() {
        if (!controllerInitialized) {
            startServer();
            controllerInitialized = true;
        }
        if (!isConnected) {
            startConnection();
        }
    }

    /**
     * Función que crea la vista para la edición del layout del Joystick.
     *
     * @return mainLayout
     */
    @SuppressLint({"ClickableViewAccessibility", "UseCompatLoadingForDrawables"})
    private View crearJoystickViewSoloVisual() {
        FrameLayout mainLayout = new FrameLayout(this);
        mainLayout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Vista del joystick (solo visual)
        JoystickView previewJoystick = getPreviewJoystick(mainLayout);

        // Botón A
        Button fakeButton = new Button(this);
        FrameLayout.LayoutParams buttonParams = new FrameLayout.LayoutParams(200, 200);
        restaurarPosiciones(null, buttonParams);  // Restaurar posición del botón
        buttonParams.rightMargin = buttonAX;
        buttonParams.bottomMargin = buttonAY;
        fakeButton.setLayoutParams(buttonParams);
        fakeButton.setBackground(getDrawable(R.drawable.action_button));
        fakeButton.setText("A");
        fakeButton.setTextSize(35);
        fakeButton.setTextColor(Color.WHITE);
        fakeButton.setGravity(Gravity.CENTER);

        fakeButton.setOnTouchListener(new TouchResizeAndDragListener(this, fakeButton));

        // Botón de guardar
        ImageButton fakeSettings = new ImageButton(this);
        FrameLayout.LayoutParams settingsParams = new FrameLayout.LayoutParams(150, 150);
        settingsParams.gravity = Gravity.TOP | Gravity.END;
        settingsParams.setMargins(0, 50, 50, 0);
        fakeSettings.setLayoutParams(settingsParams);
        fakeSettings.setBackground(null);
        fakeSettings.setPadding(5, 5, 5, 5);
        fakeSettings.setScaleType(ImageButton.ScaleType.FIT_CENTER);
        fakeSettings.setImageResource(R.drawable.guardar);

        fakeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joystickX = (int) previewJoystick.getJoystickCenterX();
                joystickY = (int) previewJoystick.getJoystickCenterY();

                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) fakeButton.getLayoutParams();
                buttonAX = fakeButton.getLeft();
                buttonAY = fakeButton.getTop();

                Log.d("POSICIÓN BOTÓN: ", buttonAX + ", " + buttonAY);

                // Re-crear el layout con las coordenadas correctas
                crearLayoutJoystick(); // Aquí cargas el layout de uso normal
            }
        });

        // Añadir todos los elementos al layout principal
        mainLayout.addView(previewJoystick);
        mainLayout.addView(fakeButton);
        mainLayout.addView(fakeSettings);

        return mainLayout;
    }

    /**
     * Función que obtiene el joystickview para su edición
     *
     * @param mainLayout
     * @return JoystickView
     */
    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    private JoystickView getPreviewJoystick(FrameLayout mainLayout) {
        JoystickView previewJoystick = new JoystickView(this);
        FrameLayout.LayoutParams joystickParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        previewJoystick.setLayoutParams(joystickParams);

        // Restaurar centro del joystick (coordenadas absolutas que guardaste)
        previewJoystick.setJoystickCenter(joystickX, joystickY);

        previewJoystick.setOnTouchListener(new View.OnTouchListener() {
            float offsetX, offsetY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int[] location = new int[2];
                mainLayout.getLocationOnScreen(location);
                float parentX = event.getRawX() - location[0];
                float parentY = event.getRawY() - location[1];

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        offsetX = parentX;
                        offsetY = parentY;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        previewJoystick.setJoystickCenter(parentX, parentY); // Mover el joystick
                        joystickX = (int) parentX; // Actualiza la posición del joystick
                        joystickY = (int) parentY; // Actualiza la posición del joystick
                        return true;

                    case MotionEvent.ACTION_UP:
                        joystickX = (int) parentX; // Guarda la posición final del joystick
                        joystickY = (int) parentY;
                        return true;
                }
                return false;
            }
        });
        return previewJoystick;
    }


    /**
     * Función para restaurar las posiciones de los botones del layout del joystick.
     *
     * @param joystickParams
     * @param buttonParams
     */
    private void restaurarPosiciones(FrameLayout.LayoutParams joystickParams, FrameLayout.LayoutParams buttonParams) {
        if (joystickParams != null) {
            joystickParams.leftMargin = joystickX;
            joystickParams.topMargin = joystickY;
        }
        if (buttonParams != null) {
            buttonParams.leftMargin = buttonAX;
            buttonParams.topMargin = buttonAY;
        }
    }

    /**
     * Función para iniciar la conexión con el servidor del juego
     */
    private void startConnection() {
        Thread connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Inicia la conexión al servidor
                discoverPCAndConnect();
            }
        });
        connectionThread.start();
    }

    /**
     * Función para encontrar la ip del servidor del juego.
     */
    private void discoverPCAndConnect() {
        new Thread(() -> {
            WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiManager.MulticastLock lock = wifi.createMulticastLock("lock");
            lock.setReferenceCounted(true);
            lock.acquire();
            try (DatagramSocket socket = new DatagramSocket(9999)) {
                socket.setSoTimeout(10000); // Espera hasta 10 segundos
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                Log.d("DISCOVERY", "Esperando broadcast de PC...");
                while (!isConnected) {
                    try {
                        socket.receive(packet);  // Intentar recibir el paquete
                        String mensaje = new String(packet.getData(), 0, packet.getLength());
                        Log.d("BROADCAST!!!!", mensaje);
                        if (mensaje.equals("PC_HOLA")) {
                            String pcIP = packet.getAddress().getHostAddress();
                            Log.d("DISCOVERY", "IP del PC detectada: " + pcIP);
                            initConnection(pcIP);  // ← tu método para conectar
                            isConnected = true;
                            Log.d("CONECTADO!!", isConnected + "");
                        }
                    } catch (SocketTimeoutException e) {
                        // Si se ha agotado el tiempo de espera, puedes intentar nuevamente
                        Log.d("DISCOVERY", "Tiempo de espera agotado. Reintentando...");
                    }
                }
                startSendingVector();
            } catch (IOException e) {
                Log.e("DISCOVERY", "No se pudo recibir broadcast: " + e.getMessage());
            }
        }).start();
    }


    /**
     * Función para inciar la conexión con la ip del servidor que nos pasan por parámetro.
     *
     * @param pcIp
     */
    public void initConnection(String pcIp) {
        Log.d("InitConection", "Conectando...");

        try {
            this.socket = new Socket(pcIp, 10000);  // Conectar al servidor
            // Crear ObjectOutputStream primero
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            Log.d("InitConection", "Conexión establecida con el servidor.");
        } catch (IOException e) {
            Log.e("Error", "Error al conectar: " + e.getMessage());
        }
    }

    /**
     * Función para iniciar el servidor del mando
     */
    private void startServer() {
        server = new CommsController(this);
        Thread serverTh = new Thread(server);
        serverTh.start();
    }

    /**
     * Función para que el mando (teléfono) vibre.
     */
    public void vibration() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long vibrate[] = {0, 100, 200, 300};
        vibrator.vibrate(vibrate, -1);
    }

    public void setVidaJoystick() {

    }

    /**
     * Función que sirve para enviar el objeto Vector velocidad que se calcula en el Joystick.
     */
    private void startSendingVector() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d("OOS NULL? ", (oos == null) + "");
                Log.d("JOYSTICK?", isJoystickView + "");
                Log.d("IS CONNECTED? ", isConnected + "");
                while (isJoystickView) {
                    try {
                        if (oos != null && isConnected) {
                            // Obtener los valores de la velocidad desde el joystick
                            Vector velocidad = joystickView.getVelocity();
                            Message msg = new Message("velocidad_nave", velocidad);
                            //Log.d("ENVIANDO...", msg.toGson());
                            sendMessageGson(msg.toGson());
                        }
                        // Espera de 16ms antes de enviar el siguiente mensaje
                        Thread.sleep(16);
                    } catch (InterruptedException e) {
                        Log.e("Error", "Error al esperar en el hilo: " + e.getMessage());
                        break; // Salir del bucle si hay una excepción
                    }
                }
            }
        }).start(); // Inicia el hilo
    }

    /**
     * Función para enviar un mensaje al cliente.
     * Es necesario crear un hilo porque Android Studio no permite operaciones de red dentro
     * del hilo principal. Por lo tanto, para enviar el mensaje, creo un hilo que envía el mensaje.
     *
     * @param msg
     */
    public void sendMessageGson(String msg) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (oos) {
                    try {
                        if (oos != null) {
                            Log.d("MENSAJE: ", msg);
                            oos.writeObject(msg);
                            oos.flush();
                        }
                    } catch (IOException e) {
                        Log.e("Error", "Error al enviar el mensaje: " + e.getMessage());
                    }
                }
            }
        }).start();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (oos != null) oos.close();
            if (socket != null) socket.close();
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
        } catch (IOException e) {
            Log.e("Error", "Error al cerrar el socket: " + e.getMessage());
        }
    }

    /**
     * Función que indica que la partida ha finalizado
     * y nos da la opción de cerrar el juego o de reiniciar la partida.
     */
    public void gameOver() {
        new AlertDialog.Builder(this).setTitle("Game Over").setMessage("¿Qué deseas hacer?").setCancelable(false).setPositiveButton("Reiniciar", (dialog, which) -> reiniciarJuego()).setNegativeButton("Salir", (dialog, which) -> salir()).show();
    }

    /**
     * Función para salir del mando
     */
    public void salir() {
        new Thread(() -> {
            try {
                if (oos != null) {
                    String exitMsg = new Message("EXIT", "EXIT").toGson();
                    Log.d("MENSAJE: ", exitMsg);
                    synchronized (oos) {
                        oos.writeObject(exitMsg);
                        oos.flush();
                    }
                    oos.close();
                    socket.close();
                    controllerInitialized = false;
                    isConnected = false;
                    server.close();
                    Thread.sleep(500);
                    startConnectionP2P();
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }


    /**
     * Función para reiniciar la partida
     */
    private void reiniciarJuego() {
        Log.d("REINICIAR!!!!!", "REINICIAR!!!!!");
        sendMessageGson(new Message("reset", "reset").toGson());
        initRemoteController();
    }

    public void setCuentaAtrasMilis(float cuentaAtrasMilis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer((long) cuentaAtrasMilis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                textoCuentaAtras.setText(timeFormatted);
            }

            @Override
            public void onFinish() {
                textoCuentaAtras.setText("00:00");
            }
        }.start();
    }

}
