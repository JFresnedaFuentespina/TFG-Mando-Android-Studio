/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.comms;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import tfgbola.TFGMoverObjetosMando;
import tfgbola.main.objects.Message;
import tfgbola.main.objects.Vector;

public class CommsController implements Runnable {

    private ServerSocket serverSocket;
    private Socket socket, socketRemoteController;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private TFGMoverObjetosMando main;

    public CommsController(TFGMoverObjetosMando aThis) {
        try {
            main = aThis;
            this.serverSocket = new ServerSocket(10000);
            broadcastToAndroid();
            connectWithRemoteController();
        } catch (IOException ex) {
            Logger.getLogger(CommsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void connectWithRemoteController() {
        String ipServer = discoverAndroidIp(); // IP del servidor AndroidStudio
        if (ipServer == null) {
            System.out.println("CONEXIÓN NO ESTABLECIDA! "
                    + "NO SE PUDO ENCONTRAR LA IP DEL SERVIDOR ANDROID");
            connectWithRemoteController();
        }
        try {
            System.out.println("Conectando...");
            this.socketRemoteController = new Socket(ipServer, 11000);
            oos = new ObjectOutputStream(socketRemoteController.getOutputStream());
            oos.flush();
            System.out.println("Conexión establecida con el servidor en el puerto 11000!");
            sendMessage("vibration", "vibration");
        } catch (IOException ex) {
            System.out.println("CONEXIÓN NO ESTABLECIDA!!!!");
            connectWithRemoteController();
        }
    }

    public void sendMessage(String type, String msg) {
        Message message = new Message(type, msg);
        if (oos != null) {
            try {
                oos.writeObject(message.toGson());
                oos.flush();
                System.out.println("Mensaje enviado correctamente.");
            } catch (IOException ex) {
                Logger.getLogger(CommsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void acceptClient() {
        try {
            System.out.println("Servidor escuchando en el puerto 10000...");
            socket = serverSocket.accept();  // Aceptar conexión del cliente
            System.out.println("Conexión aceptada: " + socket.getInetAddress());
            ois = new ObjectInputStream(socket.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(CommsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String discoverAndroidIp() {
        try (DatagramSocket socket = new DatagramSocket(8888)) {
            socket.setSoTimeout(10000); // Espera hasta 10 segundos
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            System.out.println("Esperando broadcast desde Android...");
            socket.receive(packet);  // Bloquea hasta recibir

            String mensaje = new String(packet.getData(), 0, packet.getLength());
            if (mensaje.equals("ANDROID_HOLA")) {
                String ipDescubierta = packet.getAddress().getHostAddress();
                System.out.println("Mensaje de Android recibido. IP: " + ipDescubierta);
                return ipDescubierta;
            }
        } catch (IOException e) {
            System.out.println("Error al recibir broadcast: " + e.getMessage());
        }
        return null;
    }

    public void broadcastToAndroid() {
        try (DatagramSocket datagramSocket = new DatagramSocket()) {
            datagramSocket.setBroadcast(true);
            String mensaje = "PC_HOLA";
            byte[] buffer = mensaje.getBytes();
            DatagramPacket packet = new DatagramPacket(
                    buffer, buffer.length, InetAddress.getByName("255.255.255.255"), 9999
            );
            datagramSocket.send(packet);
            System.out.println("Broadcast enviado a Android: PC_HOLA");
        } catch (IOException e) {
            System.out.println("Error enviando broadcast a Android: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        while (true) {
            acceptClient();  // Aceptar la conexión del cliente
            while (true) {
                if (ois != null) {
                    try {
                        // Leer la cadena JSON desde el ObjectInputStream
                        String jsonMessage = (String) ois.readObject();

                        // Convertir la cadena JSON en un JsonObject
                        Gson gson = new Gson();
                        JsonObject jsonObject = JsonParser.parseString(jsonMessage).getAsJsonObject();

                        // Extraer el campo "type"
                        String type = jsonObject.get("type").getAsString();

                        switch (type) {
                            case "velocidad_nave": // Extraer el objeto "obj" y convertirlo en un Vector
                                Vector velocidad = gson.fromJson(jsonObject.get("obj"), Vector.class);
                                // Imprimir los valores de velocidad
                                main.getVelocidadNave(velocidad);
                                break;
                            case "action":
                                main.setAction(gson.fromJson(jsonObject.get("obj"), String.class));
                                break;
                            case "angle_car":
                                //System.out.println("ANGULO DE ROTACION: " + gson.fromJson(jsonObject.get("obj"), String.class));
                                main.setCarAngle(Float.parseFloat(gson.fromJson(jsonObject.get("obj"), String.class)));
                                break;
                            case "velocidad_car":
                                //System.out.println("VELOCIDAD DEL COCHE: " + gson.fromJson(jsonObject.get("obj"), String.class));
                                main.setCarVelocidad(Float.parseFloat(gson.fromJson(jsonObject.get("obj"), String.class)));
                                break;
                            case "reset":
                                System.out.println("RESET!!");
                                main.reiniciar();
                                break;
                            case "EXIT":
                                System.out.println("EXIT!!");
                                this.close();
                        }

                    } catch (EOFException e) {
                        System.out.println("Cliente desconectado.");
                        break;  // Salir del loop y esperar un nuevo cliente
                    } catch (SocketException e) {
                        System.out.println("Conexión cerrada abruptamente.");
                        break;
                    } catch (IOException | ClassNotFoundException e) {
                        System.out.println("Error al recibir el objeto: " + e.getMessage());
                    }
                }
                try {
                    Thread.sleep(5);
                } catch (InterruptedException ex) {
                    Logger.getLogger(CommsController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void close() {
        try {
            this.oos.close();
            this.ois.close();
            this.socket.close();
            this.socketRemoteController.close();
            this.main.close();
        } catch (IOException ex) {
            Logger.getLogger(CommsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
