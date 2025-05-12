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

/**
 *
 * @author Nitropc
 */
public class Asteroide extends VODynamic implements Runnable {

    public Asteroide() {
        super();
        try {
            BufferedImage img = ImageIO.read(getClass().getResource("/resources/asteroide.png"));
            super.setImage(img);

            this.radio = Math.random() * 30 + 10;

            // Tamaño del área de juego
            int screenWidth = 800;
            int screenHeight = 800;
            int centerX = screenWidth / 2;
            int centerY = screenHeight / 2;

            // Generar asteroide en un borde aleatorio
            double posX, posY;
            int edge = (int) (Math.random() * 4); // 0 = arriba, 1 = abajo, 2 = izquierda, 3 = derecha

            switch (edge) {
                case 0: // Arriba
                    posX = Math.random() * screenWidth;
                    posY = 0;
                    break;
                case 1: // Abajo
                    posX = Math.random() * screenWidth;
                    posY = screenHeight;
                    break;
                case 2: // Izquierda
                    posX = 0;
                    posY = Math.random() * screenHeight;
                    break;
                default: // Derecha
                    posX = screenWidth;
                    posY = Math.random() * screenHeight;
                    break;
            }

            setPosicion(new Vector(posX, posY));

            // Calcular la velocidad hacia el centro
            double dx = centerX - posX;
            double dy = centerY - posY;
            double magnitude = Math.sqrt(dx * dx + dy * dy); // Distancia al centro

            // Normalizar el vector de dirección y multiplicarlo por una velocidad aleatoria
            double speed = Math.random() * 5 + 2; // Velocidad entre 2 y 7
            double velX = (dx / magnitude) * speed;
            double velY = (dy / magnitude) * speed;

            setVelocidad(new Vector(velX, velY));

        } catch (IOException ex) {
            Logger.getLogger(Asteroide.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void mover() {
        this.setPosicion(this.getPosicion().add(this.getVelocidad())); // Suma la velocidad a la posición
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
}
