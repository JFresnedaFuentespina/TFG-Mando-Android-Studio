package com.example.mando;

import android.util.Log;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

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
    private volatile boolean shouldRun;


    private MainActivity main;
    public CommsController(MainActivity main) {
        try {
            this.shouldRun = true;
            setConnected(false);
            this.main = main;
            this.serverSocket = new ServerSocket();
            this.serverSocket.setReuseAddress(true);
            this.serverSocket.bind(new InetSocketAddress(11000));
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
    private boolean readMessage() {
        if (ois == null || !shouldRun) return false;
        try {
            String jsonMessage = (String) ois.readObject();
            JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();
            String type = jsonObject.get("type").getAsString();
            String obj = jsonObject.get("obj").getAsString();
            switch (type) {
                case "vibration":
                    main.vibration();
                    break;
                case "aumentar_contador":
                    break;
                case "GAME_OVER":
                    main.runOnUiThread(() -> main.gameOver());
                    break;
                case "vida_inicial":
                    main.setVidaMaxima((int) Double.parseDouble(obj));
                    break;
                case "vida":
                    main.setLifeBar((int) Double.parseDouble(obj));
                    break;
                case "cuenta_atras":
                    main.setCuentaAtrasMilis(Float.parseFloat(obj));
                    break;
                case "new_score":
                    main.setScoreTextView(obj);
                default:
                    Log.d("NUEVO MENSAJE SIN IDENTIFICAR", type);
                    break;
            }
        } catch (SocketException se) {              // socket cerrado desde fuera
            Log.i("Comms", "Socket cerrado, deteniendo hilo");
            shouldRun = false;
            return false;
        } catch (EOFException | ClassNotFoundException e) {
            Log.w("Comms", "Flujo terminado: " + e.getMessage());
            shouldRun = false;
            return false;
        } catch (Exception e) {
            Log.e("Comms", "Fallo en lectura", e);
            return false;
        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    /**
     * Función que lanza un broadcast para encontrar al cliente, acepta la petición y
     * mientras haya conexión, lee los posibles mensajes que nos mande
     */
    @Override
    public void run() {
        new Thread(this::sendBroadcastDatagram).start();
        acceptClient();
        while (shouldRun) {
            if (!readMessage()) break;
        }
    }

    public void close() {
        shouldRun = false;
        try {
            if (ois != null) ois.close();
            if (socket != null && !socket.isClosed()) socket.close();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            Log.e("Comms", "Error al cerrar", e);
        }
    }


}
