/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JLabel;
import javax.swing.JPanel;
import tfgbola.main.objects.Asteroide;
import tfgbola.main.objects.Bala;
import tfgbola.main.objects.Car;
import tfgbola.main.objects.Nave;

/**
 *
 * @author jesus
 */
public class MainPanel extends Canvas {

    private MainViewer mainViewer;
    private Nave nave;
    private Car car;
    private ArrayList<Bala> balas;
    private ArrayList<Asteroide> asteroides;

    private int score;

    public MainPanel(MainViewer mainViewer) {
        initObjects(mainViewer);
        this.setSize(900, 900);
    }

    public void reiniciarJuego(MainViewer mainViewer) {
        initObjects(mainViewer);
    }

    public void initObjects(MainViewer mainViewer) {
        this.score = 0;
        this.mainViewer = mainViewer;
        this.balas = new ArrayList<>();
        this.asteroides = new ArrayList<>();
    }

    public void actualizarScore(int score) {
        this.score = score;
    }

    public void paint() {
        BufferStrategy bufferStrategy = this.getBufferStrategy();

        if (bufferStrategy == null) {
            this.createBufferStrategy(3); // Crea tres buffers para la estrategia
            return;
        }

        Graphics g = bufferStrategy.getDrawGraphics();

        // Limpia la pantalla
        g.clearRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Puntuación: " + score, 20, 40);

        // Dibuja la nave
        if (nave != null) {
            this.nave.pintar(g);
        } else {
            System.out.println("nave is null!!");
        }
        // Dibuja el coche
        if (car != null) {
            this.car.pintar(g);
        } else {
            System.out.println("car is null!!");
        }
        // Dibuja las balas que haya en pantalla
        for (Bala bala : balas) {
            bala.pintar(g);
        }
        // Dibuja los asteroides que haya en la pantalla
        for (Asteroide asteroide : asteroides) {
            asteroide.pintar(g);
        }

        // Muestra los gráficos
        bufferStrategy.show();

        // Libera los recursos gráficos
        g.dispose();
    }

    public void setNave(Nave nave) {
        this.nave = nave;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    void addBala(Bala bala) {
        this.balas.add(bala);
        paint();
    }

    void setBalas(ArrayList<Bala> balas) {
        this.balas = balas;
    }

    void setAsteroides(ArrayList<Asteroide> asteroides) {
        this.asteroides = asteroides;
    }
}
