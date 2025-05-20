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
import java.util.Scanner;
import java.util.logging.*;
import tfgjuego.TFGJuego;
import tfgbola.main.objects.Message;
import tfgbola.main.objects.Vector;

public class CommsController implements Runnable {

    private ServerSocket serverSocket;
    private Socket socket, socketRemoteController;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private TFGJuego main;

    private boolean connected;
    private int intentos;

    public CommsController(TFGJuego aThis) {
        try {
            this.intentos = 0;
            this.main = aThis;
            this.connected = false;
            this.serverSocket = new ServerSocket(10000);
            broadcastToAndroid();
            connectWithRemoteController();

        } catch (IOException ex) {
            Logger.getLogger(CommsController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void connectWithRemoteController() {
        while (!connected) {
            String ipServer = discoverAndroidIp(); // IP del servidor AndroidStudio
            System.out.println("IPSERVER: " + ipServer);

            if (ipServer == null) {
                System.out.println("CONEXIÓN NO ESTABLECIDA! NO SE PUDO ENCONTRAR LA IP DEL SERVIDOR ANDROID");
            } else {
                try {
                    System.out.println("Conectando con... " + ipServer);
                    this.socketRemoteController = new Socket(ipServer, 11000);
                    oos = new ObjectOutputStream(socketRemoteController.getOutputStream());
                    oos.flush();
                    System.out.println("Conexión establecida con " + ipServer + " en el puerto 11000!");
                    connected = true;
                } catch (IOException ex) {
                    System.out.println("CONEXIÓN NO ESTABLECIDA!!!!");
                }
            }
        }
    }

    // Función que envía la información inicial del mando para la plantilla del joystick
    public void sendNaveInitMessages(double vida, boolean hasScore) {
        sendMessage("vibration", "vibration");
        sendMessage("vida_inicial", vida);
    }

    // Función que envía la información inicial del mando para la plantilla del coche de carreras
    public void sendRacingCarInitMessages(float cuentaAtras) {
        sendMessage("vibration", "vibration");
        sendMessage("cuenta_atras", cuentaAtras);
    }

    // Función que envía un mensaje al mando
    public void sendMessage(String type, Object msg) {
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

    // Función para aceptar la conexión de un cliente a nuestro servidor
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

    // Función que obtiene el datagrama broadcast del servidor de Android 
    // para descubrir la Ip y establecer la conexión
    // Después de 5 intentos, si no la encuentra, se establece 
    // la ip de forma manual
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
            this.intentos++;
            System.out.println("INTENTO: " + this.intentos);
            if (this.intentos == 5) {
                this.intentos = 0;
                return insertarIpManual();
            }
        }
        return null;
    }

    // Función que lee la Ip por consola
    public String insertarIpManual() {
        String ip = "";
        System.out.println("Introduce la IP del dispositivo Android: ");
        try (Scanner scanner = new Scanner(System.in)) {
            ip = scanner.nextLine();
        }
        return ip;
    }

    // Función que envía un broadcast para el cliente en Android
    // se pueda conectar con nuestro servidor
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
                                main.reiniciar();
                                break;
                            case "EXIT":
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

    // Función para cerrar la conexión
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
