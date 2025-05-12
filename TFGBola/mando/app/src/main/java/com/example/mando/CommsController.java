package com.example.mando;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Clase para la gestión de los mensajes que recibe la app
 *
 * @author Jesús
 */
public class CommsController implements Runnable {
    private ServerSocket serverSocket;
    private Socket socket;
    private ObjectInputStream ois;

    private boolean isConnected;

    MainActivity main;

    public CommsController(MainActivity main) {
        try {
            setConnected(false);
            this.main = main;
            this.serverSocket = new ServerSocket(11000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    public synchronized void setConnected(boolean connected) {
        isConnected = connected;
    }

    /**
     * Función que acepta la conexión de un cliente al servidor
     */
    public void acceptClient() {
        Log.d("SERVIDOR", "Servidor escuchando en el puerto 11000...");
        try {
            socket = serverSocket.accept();
            ois = new ObjectInputStream(socket.getInputStream());
            setConnected(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Función que envía un DatagramSocket con el mensaje "ANDROID_HOLA" haciendo un broadcast
     * para que el cliente sepa la ip de nuestro servidor.
     */
    private void sendBroadcastDatagram() {
        try {
            DatagramSocket datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);
            String mensaje = "ANDROID_HOLA";
            byte[] buffer = mensaje.getBytes();

            while (socket == null) { // Solo mientras no haya conexión
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 8888);
                datagramSocket.send(packet);
                Thread.sleep(2000); // Enviar cada 2 segundos
            }

            datagramSocket.close();
        } catch (Exception e) {
            Log.e("UDP_BROADCAST", "Error al enviar broadcast", e);
        }
    }

    /**
     * Función que recibe un mensaje y lo según su tipo, ejecuta otras funciones
     */
    private void readMessage() {
        if (ois != null) {
            try {
                String jsonMessage = (String) ois.readObject();
                JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();
                String type = jsonObject.get("type").getAsString();
                switch (type) {
                    case "vibration":
                        main.vibration();
                        break;
                    case "aumentar_contador":
                        break;
                    case "GAME_OVER":
                        main.runOnUiThread(() -> main.gameOver());
                        break;
                    default:
                        Log.d("MENSAJE ERRÓNEO", "Mensaje erróneo.");
                        break;
                }
            } catch (Exception e) {
                Log.e("ERROR_MENSAJE", "Fallo en lectura del mensaje", e);
            }
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Función que lanza un broadcast para encontrar al cliente, acepta la petición y
     *  mientras haya conexión, lee los posibles mensajes que nos mande
     */
    @Override
    public void run() {
        new Thread(this::sendBroadcastDatagram).start();
        acceptClient();
        while (isConnected()) {
            readMessage();
        }
    }

    public void close() {
        try {
            setConnected(false);
            this.ois.close();
            this.socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
