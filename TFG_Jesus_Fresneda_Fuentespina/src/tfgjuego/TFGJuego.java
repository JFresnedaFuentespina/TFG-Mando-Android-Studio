/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package tfgjuego;

import tfgbola.comms.CommsController;
import tfgbola.main.MainController;
import tfgbola.main.objects.Vector;

/**
 *
 * @author jesus
 */
public class TFGJuego {

    private CommsController comms;
    private MainController main;

    public TFGJuego() {
        comms = new CommsController(this);
        main = new MainController(this);
        Thread thComms = new Thread(comms);
        thComms.start();
        sendNaveInitMessages();
        sendRacingCarInitMessages();
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
    
    public void sendVidaNave(int vida){
        this.comms.sendMessage("vida", vida);
    }
    
    public void sendNaveInitMessages(){
        this.comms.sendNaveInitMessages(this.main.getVidaNave(), true);
    }
    
    public void sendRacingCarInitMessages(){
        this.comms.sendRacingCarInitMessages(this.main.getCuentaAtras());
    }

    public static void main(String[] args) {
        // TODO code application logic here
        new TFGJuego();
    }

    public void close() {
        System.exit(0);
    }

    public void sendScore(int score) {
        this.comms.sendMessage("new_score", score);
    }

}
