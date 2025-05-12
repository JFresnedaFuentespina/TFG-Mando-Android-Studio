package tfgbola.main.objects;

import java.awt.image.BufferedImage;

public class VODynamic extends VisualObject {

    private Vector posicion;
    private Vector velocidad;
    private Vector aceleracion;
    private boolean isAlive;

    public double radio;
    public double anguloRotacion;
    public double anguloRotacionPersistente;

    public VODynamic() {
        super();
        this.isAlive = true;
        this.posicion = new Vector();
        this.velocidad = new Vector(0, 0);
        this.aceleracion = new Vector(0, 0);
        this.anguloRotacion = 0.0;
        this.anguloRotacionPersistente = 0.0;
    }

    public VODynamic(BufferedImage img) {
        super(img);
        this.isAlive = true;
        this.posicion = new Vector();
        this.velocidad = new Vector(0, 0);
        this.aceleracion = new Vector(0, 0);
        this.anguloRotacion = 0.0;
        this.anguloRotacionPersistente = 0.0;
    }

    public Vector getPosicion() {
        return posicion;
    }

    public void setPosicion(Vector posicion) {
        this.posicion = posicion;
    }

    public Vector getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(Vector velocidad) {
        this.velocidad = velocidad;
    }

    public Vector getAceleracion() {
        return aceleracion;
    }

    public void setAceleracion(Vector aceleracion) {
        this.aceleracion = aceleracion;
    }

    // Método para calcular el ángulo de dirección de la velocidad
    public double calcularAngulo() {
        return 0;
    }

    public double getRadio() {
        return radio;
    }

    public void setRadio(double radio) {
        this.radio = radio;
    }

    public double getDiam() {
        return this.radio * 2;
    }

    public boolean isIsAlive() {
        return isAlive;
    }

    public void setIsAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    // Método para rotar el objeto
    public void rotate() {
    }

    public void mover() {
    }

    public void damage(VODynamic destino) {
        destino.explode();
    }

    public void explode() {
        this.isAlive = false;
    }

    public void rebotarX() {
    }

    public void rebotarY() {
    }

    public void sendBola() {
    }

    public void disparar() {
    }

    @Override
    public BufferedImage getImage() {
        return super.getImage();
    }

    @Override
    public void setImage(BufferedImage img) {
        super.setImage(img);
    }

    public double getAnguloRotacion() {
        return anguloRotacion;
    }

    public void setAnguloRotacion(double anguloRotacion) {
        this.anguloRotacion = anguloRotacion;
    }
    
    
}
