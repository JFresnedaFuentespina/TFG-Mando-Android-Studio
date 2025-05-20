/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main;

import java.util.ArrayList;
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

    // Función para calcular la nueva posición de la nave
    public Vector calcNewPositions(VODynamic obj) {
        Vector oldPosition = obj.getPosicion();       // Posición anterior
        Vector velocity = obj.getVelocidad();         // Velocidad
        Vector acceleration = obj.getAceleracion();   // Aceleración

        double deltaTime = 0.5;

        // Calcular el desplazamiento por velocidad
        Vector velocityDisplacement = velocity.multiply(deltaTime);

        // Calcular el desplazamiento por aceleración
        Vector accelerationDisplacement = acceleration.multiply(0.5 * deltaTime * deltaTime);

        // Calcular la nueva posición
        Vector newPosition = oldPosition.add(velocityDisplacement).add(accelerationDisplacement);

        return newPosition;
    }

    // Función para calcular la nueva posición del coche
    public Vector calcNewPositionsCar(Car obj) {
        Vector oldPosition = obj.getPosicion();
        float velocidad = obj.getVelocidadCar();
        float angulo = obj.getAngulo();
        double deltaTime = 0.5;

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

        return restrictedPosition;
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

    public void setCarVelocidad(float velocidad) {
        this.car.setVelocidadCar(velocidad);
    }
}
