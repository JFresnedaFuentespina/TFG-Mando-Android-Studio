/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package tfgbola.main.objects;

import com.google.gson.Gson;

/**
 *
 * @author Jesús Fresneda Fuentespina
 */
public class Message {

    String type;
    Object obj;

    public Message(String type, Object obj) {
        this.type = type;
        this.obj = obj;
    }

    public String toGson() {
        Gson msg = new Gson();
        return msg.toJson(this);
    }

    @Override
    public String toString() {
        return "Message{"
                + "type='" + type + '\''
                + ", obj=" + obj
                + '}';
    }
}
