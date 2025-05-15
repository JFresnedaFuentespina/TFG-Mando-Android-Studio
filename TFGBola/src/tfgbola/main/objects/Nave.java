/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main.objects;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.Logger;
import tfgbola.main.MainModel;

/**
 *
 * @author jesus
 */
public class Nave extends VODynamic implements Runnable {

    private double radio;

    private int vida;

    public static final Vector VELOCIDAD_MAXIMA = new Vector(4, 4);

    private MainModel model;

    public Nave() {
        super();
        this.radio = 15;
        this.vida = 100;
    }

    public double getRadio() {
        return radio;
    }

    public void setRadio(double radio) {
        this.radio = radio;
    }

    public void setModel(MainModel model) {
        this.model = model;
    }

    @Override
    public Vector getPosicion() {
        return super.getPosicion();
    }

    @Override
    public void setPosicion(Vector posicion) {
        super.setPosicion(posicion);
    }

    public double getDiam() {
        return (this.radio * 2);
    }

    public synchronized void damage(double dmg) {
        this.vida -= dmg;
        if(this.vida <= 0){
            this.vida = 0;
            this.explode();
        }
    }

    public int getVida() {
        return vida;
    }

    public void setVida(int vida) {
        this.vida = vida;
    }

    @Override
    public void disparar() {
        // Obtener la velocidad actual de la nave
        Vector velocidadBala = this.getVelocidad();
        velocidadBala.setX(velocidadBala.getX() * 0.7);
        velocidadBala.setY(velocidadBala.getY() * 0.7);
        // Si la nave está quieta, usar la dirección basada en el ángulo de rotación
        if (velocidadBala.getX() == 0 && velocidadBala.getY() == 0) {
            double velocidadMagnitud = 2;
            // Convertir el ángulo a radianes
            double anguloRad = Math.toRadians(this.anguloRotacion - 90);
            // Calcular los componentes de la velocidad
            double velX = velocidadMagnitud * Math.cos(anguloRad);
            double velY = velocidadMagnitud * Math.sin(anguloRad);
            // Crear el nuevo vector de velocidad para la bala
            velocidadBala = new Vector(velX, velY);
        }
        // Crear la bala con la velocidad ajustada
        Bala bala = new Bala(velocidadBala);
        bala.setModel(model);
        bala.setPosicion(this.getPosicion());
        // Añadir la bala al modelo
        this.model.addBala(bala);
        // Iniciar el hilo de la bala
        Thread thBala = new Thread(bala);
        thBala.start();
    }

    @Override
    public double calcularAngulo() {
        // Verifica que la velocidad no esté en (0, 0)
        if (getVelocidad().getX() != 0 || getVelocidad().getY() != 0) {
            // Calcula el ángulo en radianes usando atan2 (y, x)
            double angulo = Math.atan2(getVelocidad().getY(), getVelocidad().getX());
            angulo += Math.PI / 2;
            anguloRotacionPersistente = angulo;
            return angulo;
        }
        return anguloRotacionPersistente; // Si la velocidad es 0, no se rota
    }

    @Override
    public void rotate() {
        // Calcular el ángulo de la dirección del movimiento
        double angulo = calcularAngulo();
        // Convertir de radianes a grados
        this.anguloRotacion = Math.toDegrees(angulo);
    }

    public void setAction(String action) {
        switch (action) {
            case "dispara":
                disparar();
                break;
            default:
                System.out.println("ACCIONES ADICIONALES SIN ESPECIFICAR...");
                break;
        }
    }

    @Override
    public void pintar(Graphics g) {
        if (super.getImage() != null) {
            // Asegurarse de que el ángulo está actualizado
            rotate();  // Llamar al método para actualizar el ángulo de rotación

            // Calcular la posición de la imagen
            int posicionX = (int) (this.getPosicion().getX() - this.getRadio());
            int posicionY = (int) (this.getPosicion().getY() - this.getRadio());

            // Crear un Graphics2D para poder hacer rotaciones
            Graphics2D g2d = (Graphics2D) g;

            // Guardar el estado original de la transformación
            AffineTransform originalTransform = g2d.getTransform();

            // Rotar la imagen alrededor de su centro
            g2d.rotate(Math.toRadians(this.anguloRotacion), this.getPosicion().getX(), this.getPosicion().getY());

            // Dibujar la imagen rotada
            g2d.drawImage(super.getImage(), posicionX, posicionY, (int) this.getDiam(), (int) this.getDiam(), null);

            // Restaurar el estado original de la transformación para evitar afectar otros objetos
            g2d.setTransform(originalTransform);
        }
        mover();  // Mueve el objeto
    }

    @Override
    public synchronized void mover() {
        if (Math.abs(getVelocidad().getX()) >= VELOCIDAD_MAXIMA.getX()) {
            if (getVelocidad().getX() > 0) {
                getVelocidad().setX(VELOCIDAD_MAXIMA.getX());
            } else {
                getVelocidad().setX(VELOCIDAD_MAXIMA.getX() * -1);
            }
        }
        if (Math.abs(getVelocidad().getY()) >= VELOCIDAD_MAXIMA.getX()) {
            if (getVelocidad().getY() > 0) {
                getVelocidad().setY(VELOCIDAD_MAXIMA.getY());
            } else {
                getVelocidad().setY(VELOCIDAD_MAXIMA.getY() * -1);
            }
        }
        super.setPosicion(model.calcNewPositions(this));
    }

    @Override
    public void run() {
        while (this.isIsAlive()) {
            try {
                mover();
                Thread.sleep(16);
            } catch (InterruptedException ex) {
                Logger.getLogger(Nave.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
