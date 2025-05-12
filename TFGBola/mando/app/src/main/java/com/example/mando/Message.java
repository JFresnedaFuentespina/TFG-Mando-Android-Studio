package com.example.mando;

import com.google.gson.Gson;

/**
 *  Clase para la gestión de los mensajes que se envian entre cliente y servidor.
 */
public class Message {
    String type;
    Object obj;

    public Message(String type, Object obj){
        this.type = type;
        this.obj = obj;
    }

    public String toGson(){
        Gson msg = new Gson();
        return msg.toJson(this);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", obj=" + obj +
                '}';
    }
}
