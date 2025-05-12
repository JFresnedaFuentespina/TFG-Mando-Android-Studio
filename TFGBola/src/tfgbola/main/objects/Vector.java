package tfgbola.main.objects;

import java.io.Serializable;

public class Vector implements Serializable {

    private double x, y;

    public Vector() {
        x = 100;
        y = 100;
    }

    public Vector(double x, double y) {
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

    public Vector add(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }

    // Método para multiplicar un vector por un escalar
    public Vector multiply(double scalar) {
        return new Vector(this.x * scalar, this.y * scalar);
    }

    public double distanceTo(Vector other) {
        double deltaX = this.x - other.x;
        double deltaY = this.y - other.y;
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    @Override
    public String toString() {
        return "Vector{"
                + "x=" + x
                + ", y=" + y
                + '}';
    }
}
