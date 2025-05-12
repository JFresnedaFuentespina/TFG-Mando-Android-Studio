/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main;

import java.util.ArrayList;
import tfgbola.TFGMoverObjetosMando;
import tfgbola.main.objects.Asteroide;
import tfgbola.main.objects.Bala;
import tfgbola.main.objects.Car;
import tfgbola.main.objects.Nave;
import tfgbola.main.objects.Vector;

/**
 *
 * @author jesus
 */
public class MainController {

    private TFGMoverObjetosMando tfg;

    private MainViewer viewer;
    private MainModel model;

    public MainController(TFGMoverObjetosMando aThis) {
        this.tfg = aThis;
        this.model = new MainModel(this);
        this.viewer = new MainViewer(this);
        Thread viewerTh = new Thread(this.viewer);
        viewerTh.start();
    }

    public void reiniciarJuego(){
        this.model = new MainModel(this);
        this.viewer.reiniciarJuego(this);
    }

    public MainModel getModel() {
        return this.model;
    }

    public Nave getBola() {
        return this.model.getNave();
    }

    public void setVelocidadNave(Vector velocidad) {
        this.model.setVelocidadNave(velocidad);
    }

    public void setAction(String action) {
        this.model.setAction(action);
    }
    
    public Nave setNaveViewer(){
        return this.model.getNave();
    }
    
    public Car setCarViewer(){
        return this.model.getCar();
    }

    public void addBala(Bala bala) {
        this.viewer.addBalaToPanel(bala);
    }

    public void addAsteroide(Asteroide asteroide) {
        this.model.addAsteroide(asteroide);
    }

    void setAsteroides(ArrayList<Asteroide> asteroides) {
        this.viewer.setAsteroides(asteroides);
    }

    void setBalas(ArrayList<Bala> balas) {
        this.viewer.setBalas(balas);
    }

    public void setCarAngle(float angulo) {
        this.model.setCarAngle(angulo);
    }
    
    public void setCarVelocidad(float velocidad) {
        this.model.setCarVelocidad(velocidad);
    }

    public void sendGameOver() {
        this.tfg.sendGameOver();
    }
}
