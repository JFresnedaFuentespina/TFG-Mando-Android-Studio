/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package tfgbola;

import tfgbola.comms.CommsController;
import tfgbola.main.MainController;
import tfgbola.main.objects.Vector;

/**
 *
 * @author jesus
 */
public class TFGMoverObjetosMando {

    private CommsController comms;
    private MainController main;

    public TFGMoverObjetosMando() {
        comms = new CommsController(this);
        main = new MainController(this);
        Thread thComms = new Thread(comms);
        thComms.start();
    }

    public void getMsg(String msg) {
        System.out.println(msg);
    }

    public void getVelocidadNave(Vector velocidad) {
        main.setVelocidadNave(velocidad);
    }

    public void setCarVelocidad(float velocidad) {
        main.setCarVelocidad(velocidad);
    }

    public void setCarAngle(float angulo) {
        main.setCarAngle(angulo);
    }

    public void setAction(String action) {
        this.main.setAction(action);
    }

    public void sendGameOver() {
        this.comms.sendMessage("GAME_OVER", "GAME_OVER");
    }
    
    public void reiniciar(){
        this.main.reiniciarJuego();
    }

    public static void main(String[] args) {
        // TODO code application logic here
        new TFGMoverObjetosMando();
    }

    public void close() {
        System.exit(0);
    }

}
