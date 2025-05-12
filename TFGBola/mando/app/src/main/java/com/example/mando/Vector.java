package com.example.mando;

/**
 *  Clase para la gestión de la velocidad del objeto que controlamos.
 */
public class Vector {
    private double x, y;

    public Vector() {
        x = 100;
        y = 100;
    }

    public Vector(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
