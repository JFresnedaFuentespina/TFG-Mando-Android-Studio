/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main.objects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.logging.Level;
import java.util.logging.Logger;
import tfgbola.main.MainModel;

/**
 *
 * @author Nitropc
 */
public class Car extends VODynamic implements Runnable {

    private float angulo, velocidad;
    private boolean isAlive;
    public static final float VELOCIDAD_MAXIMA = 15;

    private MainModel model;

    public Car() {
        super.setCarImage();
        super.setPosicion(new Vector(200, 200));
        isAlive = true;
        super.radio = 20;
        angulo = 270;
        velocidad = 0;
    }

    public Car(float angulo, float velocidad) {
        super.setCarImage();
        super.setPosicion(new Vector(200, 200));
        this.isAlive = true;
        super.radio = 10;
        this.angulo = angulo;
        this.velocidad = velocidad;
    }

    public float getAngulo() {
        return angulo;
    }

    public void setAngulo(float angulo) {
        this.angulo = angulo;
    }

    public float getVelocidadCar() {
        return velocidad;
    }

    public void setVelocidadCar(float velocidad) {
        this.velocidad = velocidad;
    }

    @Override
    public boolean isIsAlive() {
        return isAlive;
    }

    @Override
    public void setIsAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

@Override
public void pintar(Graphics g) {
    if (super.getImage() != null) {
        // Calcular la posición de la imagen
        int posicionX = (int) (this.getPosicion().getX() - this.getRadio());
        int posicionY = (int) (this.getPosicion().getY() - this.getRadio());

        // Crear un Graphics2D para poder hacer rotaciones
        Graphics2D g2d = (Graphics2D) g;
        // Guardar el estado original de la transformación
        AffineTransform originalTransform = g2d.getTransform();
        // Rotar la imagen alrededor de su centro
        g2d.rotate(Math.toRadians(this.angulo), this.getPosicion().getX(), this.getPosicion().getY());
        // Dibujar la imagen rotada
        g2d.drawImage(super.getImage(), posicionX, posicionY, (int) this.getDiam(), (int) this.getDiam(), null);
        // Restaurar el estado original de la transformación para evitar afectar otros objetos
        g2d.setTransform(originalTransform);
    } else {
        g.setColor(Color.RED);
        g.fillOval((int) getPosicion().getX(), (int) getPosicion().getY(), 20, 20);
        System.out.println("Imagen del coche no cargada, pintando círculo rojo");
    }
    mover();  // Mueve el objeto
}

    @Override
    public synchronized void mover() {
        if (Math.abs(getVelocidadCar()) >= VELOCIDAD_MAXIMA) {
            if (getVelocidadCar() > 0) {
                setVelocidadCar(VELOCIDAD_MAXIMA);
            } else {
                setVelocidadCar(VELOCIDAD_MAXIMA * -1);
            }
        }
        super.setPosicion(model.calcNewPositionsCar(this));
    }

    @Override
    public void run() {
        while (isAlive) {
            try {
                mover();
                Thread.sleep(16);
            } catch (InterruptedException ex) {
                Logger.getLogger(Nave.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setModel(MainModel aThis) {
        this.model = aThis;
    }

}
