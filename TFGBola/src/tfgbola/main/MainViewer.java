/*

 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import tfgbola.main.objects.Asteroide;
import tfgbola.main.objects.Bala;
import tfgbola.main.objects.Car;
import tfgbola.main.objects.Nave;
import tfgbola.main.objects.Vector;

/**
 *
 * @author jesus
 */
public class MainViewer extends JFrame implements Runnable {

    private MainController mainController;
    private MainPanel mainPanel;

    private Nave nave;
    private Car car;
    private ArrayList<Bala> balas;
    private ArrayList<Asteroide> asteroides;

    public static final float CUENTA_ATRAS_MILIS = 5000;

    private int score;

    public MainViewer(MainController aThis) {
        initObjects(aThis);
        createThreads();
        addPanel();
        this.setSize(900, 900);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
    }

    // Función para añadir los objetos al panel
    public void addPanel() {
        this.mainPanel = new MainPanel(this);
        this.mainPanel.setNave(nave);
        this.mainPanel.setCar(car);
        this.add(mainPanel);
    }

    // Función para reiniciar el panel
    public void reiniciarPanel() {
        this.mainPanel.reiniciarJuego(this);
        this.mainPanel.setNave(nave);
        this.mainPanel.setCar(car);
    }

    // Función para crear los hilos que pintan los objetos en el panel
    public void createThreads() {
        // Inicializamos la nave
        this.nave = mainController.setNaveViewer();
        this.nave.setModel(this.mainController.getModel());
        Thread thNave = new Thread(nave);
        thNave.start();
        // Inicializamos el coche
        this.car = mainController.setCarViewer();
        Thread thCar = new Thread(car);
        thCar.start();
    }

    // Función para reiniciar el juego
    public void reiniciarJuego(MainController aThis) {
        initObjects(aThis);
        createThreads();
        reiniciarPanel();
    }

    // Función para inicializar los objetos que se pintan
    public void initObjects(MainController aThis) {
        this.score = 0;
        this.mainController = aThis;
        this.balas = new ArrayList<>();
        this.asteroides = new ArrayList<>();
    }

    // Función para añadir una bala al panel
    public void addBalaToPanel(Bala bala) {
        this.balas.add(bala);
        this.mainPanel.addBala(bala);
    }

    public ArrayList<Bala> getBalas() {
        return this.balas;
    }

    public Nave getNave() {
        return this.nave;
    }

    public void setNave(Nave nave) {
        this.nave = nave;
        this.mainPanel.setNave(nave);
        this.mainPanel.paint();
    }

    // Función para crear un asteroide
    public void generarAsteroide() {
        Asteroide asteroide = new Asteroide();
        asteroides.add(asteroide);
        mainController.addAsteroide(asteroide);
    }

    @Override
    public void run() {
        long lastAsteroidTime = System.currentTimeMillis();
        while (true) {
            try {
                long currentTime = System.currentTimeMillis();
                // Verificar si ha pasado el tiempo necesario para crear un nuevo asteroide
                if (currentTime - lastAsteroidTime >= (Math.random() * 3000 + 1000)) { // Entre 1s y 4s
                    generarAsteroide();
                    lastAsteroidTime = currentTime; // Reiniciar el temporizador
                }
                // Actualizar el juego: mover los objetos, verificar colisiones, etc.
                if (nave.isIsAlive()) {
                    actualizarJuego();
                }
                // Solicitar repintado de la interfaz gráfica
                this.mainPanel.paint();
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                Logger.getLogger(MainViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    // Función para actualizar los objetos del juego
    private void actualizarJuego() {
        for (Bala bala : balas) {
            bala.mover();  // Mueve las balas
        }

        // Crear listas de asteroides y balas a eliminar
        ArrayList<Bala> balasToRemove = new ArrayList<>();
        ArrayList<Asteroide> asteroidesToRemove = new ArrayList<>();

        // Verificar colisiones entre balas y asteroides
        for (Bala bala : balas) {
            boolean addscore = false;
            for (Asteroide asteroide : asteroides) {
                if (checkImpacto(bala, asteroide)) {
                    // Marcar para eliminar los asteroides y balas que han colisionado
                    asteroidesToRemove.add(asteroide);
                    balasToRemove.add(bala);
                    bala.explode();
                    asteroide.explode();
                    if (!addscore) {
                        aumentarScore();
                        addscore = true;
                    }
                }
            }
        }
        // Verificar colisión entre nave y asteroides
        for (Asteroide asteroide : asteroides) {
            if (checkImpacto(nave, asteroide)) {
                asteroidesToRemove.add(asteroide);
                this.nave.damage(asteroide.getRadio());
                this.mainController.sendVidaNave(this.nave.getVida());
                if (!this.nave.isIsAlive()) {
                    this.mainController.sendGameOver();
                    break;
                }
            }
        }

        // Verificar si alguna bala o asteroide se ha salido de los márgenes de la ventana
        for (Bala bala : balas) {
            if (isOutsideBounds(bala.getPosicion())) {
                balasToRemove.add(bala);
            }
        }

        for (Asteroide asteroide : asteroides) {
            if (isOutsideBounds(asteroide.getPosicion())) {
                asteroidesToRemove.add(asteroide);
            }
        }

        // Eliminar los asteroides y balas que han impactado o que se han salido de los márgenes
        asteroides.removeAll(asteroidesToRemove);
        balas.removeAll(balasToRemove);

        // Actualizar los objetos en el controlador
        mainController.setAsteroides(asteroides);
        mainController.setBalas(balas);
    }

    // Función para verificar si un objeto está fuera de los límites de la ventana
    private boolean isOutsideBounds(Vector posicion) {
        return posicion.getX() < 0 || posicion.getX() > 900 || posicion.getY() < 0 || posicion.getY() > 900;
    }

    public void aumentarScore() {
        this.score += 1;
        this.mainPanel.actualizarScore(score);
        this.mainController.sendScore(score);
    }

    public void actualizarScore(int score) {
        this.mainPanel.actualizarScore(score);
    }

    // Función para comprobar posibles impactos entre objetos y asteroides
    public boolean checkImpacto(Object object, Asteroide asteroide) {
        boolean impact = false;
        if (object instanceof Bala) {
            Bala bala = (Bala) object;
            // Obtener la posición de la bala y del asteroide
            Vector posBala = bala.getPosicion();
            Vector posAsteroide = asteroide.getPosicion();
            // Obtener el radio de la bala y el asteroide
            double radioBala = bala.getRadio();
            double radioAsteroide = asteroide.getRadio();
            // Calcular la distancia entre la bala y el asteroide
            double distancia = posBala.distanceTo(posAsteroide);
            // Si la distancia es menor que la suma de los radios, se considera un impacto
            impact = distancia <= (radioBala + radioAsteroide);
        } else if (object instanceof Nave) {
            Nave nave = (Nave) object;
            // Obtener la posición de la nave y del asteroide
            Vector posNave = nave.getPosicion();
            Vector posAsteroide = asteroide.getPosicion();
            // Obtener el radio de la nave y el asteroide
            double radioNave = nave.getRadio();  // Radio de la nave
            double radioAsteroide = asteroide.getRadio();// Radio del asteroide
            // Calcular la distancia entre la bala y el asteroide
            double distancia = posNave.distanceTo(posAsteroide);
            // Si la distancia es menor que la suma de los radios, se considera un impacto
            impact = distancia <= (radioNave + radioAsteroide);
        }
        return impact;
    }

    void setAsteroides(ArrayList<Asteroide> asteroides) {
        this.asteroides = asteroides;
        this.mainPanel.setAsteroides(asteroides);
        this.mainPanel.paint();
    }

    void setBalas(ArrayList<Bala> balas) {
        this.balas = balas;
        this.mainPanel.setBalas(balas);
        this.mainPanel.paint();

    }
}
