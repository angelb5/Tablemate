package pe.edu.pucp.tablemate.Entity;


import com.google.firebase.Timestamp;

public class Mensaje {
    private String mensaje;
    private String uid;
    private Timestamp timestamp;

    public Mensaje() {
    }

    public Mensaje(String mensaje, String uid, Timestamp timestamp) {
        this.mensaje = mensaje;
        this.uid = uid;
        this.timestamp = timestamp;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
