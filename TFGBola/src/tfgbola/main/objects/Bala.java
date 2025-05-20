/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main.objects;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import tfgbola.main.MainModel;

/**
 *
 * @author Nitropc
 */
public class Bala extends VODynamic implements Runnable {

    private MainModel model;

    public Bala(Vector velocidad) {
        super();
        try {
            BufferedImage img = ImageIO.read(getClass().getResource("/resources/bala.png"));
            super.setImage(img);
            this.radio = 10;
            setVelocidad(velocidad);

        } catch (IOException ex) {
            Logger.getLogger(Bala.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    @Override
    public void mover() {
        this.setPosicion(this.model.calcNewPositions(this));  // Usamos calcNewPositions para calcular la nueva posición
    }

    @Override
    public synchronized double calcularAngulo() {
        // Verifica que la velocidad no esté en (0, 0)
        if (getVelocidad().getX() != 0 || getVelocidad().getY() != 0) {
            // Calcula el ángulo en radianes usando atan2 (y, x)
            double angulo = Math.atan2(getVelocidad().getY(), getVelocidad().getX());
            angulo -= Math.PI / 2;
            anguloRotacionPersistente = angulo;
            return angulo;
        }
        return anguloRotacionPersistente; // Si la velocidad es 0, no se rota
    }

    @Override
    public synchronized void rotate() {
        // Calcular el ángulo de la dirección del movimiento
        double angulo = calcularAngulo();
        // Convertir de radianes a grados
        this.anguloRotacion = Math.toDegrees(angulo);
    }

    @Override
    public synchronized void pintar(Graphics g) {
        if (super.getImage() != null) {
            // Asegurarse de que el ángulo está actualizado
            rotate();

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
        mover();
    }

    @Override
    public void run() {
        while (this.isIsAlive()) {
            try {
                mover();
                Thread.sleep(20);
            } catch (InterruptedException ex) {
                Logger.getLogger(Nave.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void setAngulo(double anguloRotacion) {
        this.anguloRotacion = anguloRotacion;
    }

}
