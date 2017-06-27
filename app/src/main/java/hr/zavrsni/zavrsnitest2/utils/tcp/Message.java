package hr.zavrsni.zavrsnitest2.utils.tcp;

import java.io.Serializable;

public class Message implements Serializable {
    private String mac;
    private String message;

    public Message(String mac, String message) {
        this.mac = mac;
        this.message = message;
    }

    public String getMac() {
        return mac;
    }

    public String getMessage() {
        return message;
    }
}
