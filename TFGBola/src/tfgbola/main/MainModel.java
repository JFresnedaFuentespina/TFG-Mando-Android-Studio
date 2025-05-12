/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main;

import java.util.ArrayList;
import java.util.Iterator;
import tfgbola.main.objects.Asteroide;
import tfgbola.main.objects.Bala;
import tfgbola.main.objects.Car;
import tfgbola.main.objects.Nave;

import tfgbola.main.objects.VODynamic;
import tfgbola.main.objects.Vector;

/**
 *
 * @author jesus
 */
public class MainModel {

    private MainController mainController;

    private Nave nave;
    private Car car;

    private ArrayList<Bala> balas;
    private ArrayList<Asteroide> asteroides;

    public MainModel(MainController aThis) {
        mainController = aThis;
        this.nave = new Nave();
        this.car = new Car();
        this.balas = new ArrayList<>();
        this.asteroides = new ArrayList<>();
        this.nave.setModel(this);
        this.car.setModel(this);
    }

    public Vector calcNewPositions(VODynamic obj) {
        // Obtener la posición, velocidad y aceleración actuales del objeto
        Vector oldPosition = obj.getPosicion();       // Posición anterior
        Vector velocity = obj.getVelocidad();         // Velocidad
        Vector acceleration = obj.getAceleracion();   // Aceleración

        // Suponiendo un intervalo de tiempo (ajustable según lo que necesites)
        double deltaTime = 0.5;

        // Calcular el desplazamiento por velocidad
        Vector velocityDisplacement = velocity.multiply(deltaTime);

        // Calcular el desplazamiento por aceleración
        Vector accelerationDisplacement = acceleration.multiply(0.5 * deltaTime * deltaTime);

        // Calcular la nueva posición
        Vector newPosition = oldPosition.add(velocityDisplacement).add(accelerationDisplacement);

        return newPosition;
    }

public Vector calcNewPositionsCar(Car obj) {
    Vector oldPosition = obj.getPosicion();
    float velocidad = obj.getVelocidadCar();
    float angulo = obj.getAngulo();
    double deltaTime = 0.5;
    //System.out.println(">>> Velocidad: " + velocidad + " | Ángulo: " + angulo);
    
    // Convertir el ángulo a radianes
    double rad = Math.toRadians(angulo);
    
    // Calcular el desplazamiento en X y Y usando trigonometría
    double dx = velocidad * Math.cos(rad) * deltaTime;
    double dy = velocidad * Math.sin(rad) * deltaTime;

    // Crear el vector de desplazamiento
    Vector desplazamiento = new Vector(dx, dy);

    // Calcular la nueva posición
    Vector newPosition = oldPosition.add(desplazamiento);
    //System.out.println(">>> Desplazamiento: " + desplazamiento);
    
    // Restringir la nueva posición a los límites de la pantalla
    int maxWidth = 900; // Ancho máximo de la pantalla
    int maxHeight = 900; // Alto máximo de la pantalla

    // Asegurarse de que la nueva posición no se salga de los límites
    double newX = Math.min(Math.max(0, newPosition.getX()), maxWidth);
    double newY = Math.min(Math.max(0, newPosition.getY()), maxHeight);

    // Crear el nuevo vector de posición restringida
    Vector restrictedPosition = new Vector(newX, newY);
    
    //System.out.println(">>> NEW POSITION CAR (restringida): " + restrictedPosition);
    
    return restrictedPosition;
}

    // Método para resetear la nave a su estado inicial
    public void resetNave() {
        // Resetear la posición de la nave
        this.nave.setPosicion(new Vector(400, 400)); // O cualquier posición inicial deseada
        this.nave.setVelocidad(new Vector(0, 0)); // La nave está quieta al reiniciar
        this.nave.setAnguloRotacion(0); // Angulo de rotación inicial (por ejemplo)
    }

    // Método para limpiar las balas y asteroides
    public void limpiarBalasYAsteroides() {
        this.balas.clear(); // Limpiar las balas
        this.asteroides.clear(); // Limpiar los asteroides
    }

    public boolean checkImpacto(VODynamic src, VODynamic dst) {
        // Obtener la posición y el radio de la bala
        Vector posicionBala = src.getPosicion();
        double radioBala = src.getRadio();

        // Obtener la posición y el radio del asteroide
        Vector posicionDst = dst.getPosicion();
        double radioDst = dst.getRadio();

        // Calcular la distancia entre los centros de la bala y el asteroide
        double distanciaX = posicionBala.getX() - posicionDst.getX();
        double distanciaY = posicionBala.getY() - posicionDst.getY();
        double distancia = Math.sqrt(distanciaX * distanciaX + distanciaY * distanciaY);

        // Comprobar si la distancia es menor o igual a la suma de los radios (colisión)
        if (distancia <= radioBala + radioDst) {
            System.out.println("¡Colisión detectada contra un asteroide!");
            return true;
        } else {
            return false;
        }
    }

    public Nave getNave() {
        return this.nave;
    }

    public Car getCar() {
        return this.car;
    }

    public ArrayList<Bala> getBalas() {
        return balas;
    }

    void setVelocidadNave(Vector velocidad) {
        this.nave.setVelocidad(velocidad);
    }

    void setAction(String action) {
        this.nave.setAction(action);
    }

    public void addBala(Bala bala) {
        this.balas.add(bala);
        this.mainController.addBala(bala);
    }

    public void addAsteroide(Asteroide asteroide) {
        this.asteroides.add(asteroide);
    }

    public void setCarAngle(float angulo) {
        this.car.setAngulo(angulo);
    }
    
    public void setCarVelocidad(float velocidad){
        this.car.setVelocidadCar(velocidad);
    }
}
