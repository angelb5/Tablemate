package pe.edu.pucp.tablemate.Entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Reserva {

    @Exclude
    private String id;
    private String fecha;
    private String hora;
    private transient Timestamp sendTime;
    private String estado;
    private int numPersonas;
    private RUser cliente;
    private RRestaurant restaurant;
    private List<HashMap<String,String>> mensajes;

    public Reserva() {
    }

    public Reserva(String fecha, String hora, Timestamp sendTime, String estado, int numPersonas, RUser cliente, RRestaurant restaurant) {
        this.fecha = fecha;
        this.hora = hora;
        this.sendTime = sendTime;
        this.estado = estado;
        this.numPersonas = numPersonas;
        this.cliente = cliente;
        this.restaurant = restaurant;
        mensajes = new ArrayList<>();
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public Timestamp getSendTime() {
        return sendTime;
    }

    public void setSendTime(Timestamp sendTime) {
        this.sendTime = sendTime;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getNumPersonas() {
        return numPersonas;
    }

    public void setNumPersonas(int numPersonas) {
        this.numPersonas = numPersonas;
    }

    public RUser getCliente() {
        return cliente;
    }

    public void setCliente(RUser cliente) {
        this.cliente = cliente;
    }

    public RRestaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(RRestaurant restaurant) {
        this.restaurant = restaurant;
    }

    public class RUser {
        private String nombre;
        private String avatarUrl;
        private String dni;
        private String uid;

        public RUser() {
        }

        public RUser(String nombre, String avatarUrl, String dni, String uid) {
            this.nombre = nombre;
            this.avatarUrl = avatarUrl;
            this.dni = dni;
            this.uid = uid;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }
    }

    public class RRestaurant {
        private String nombre;
        private String fotoUrl;
        private String id;

        public RRestaurant() {
        }

        public RRestaurant(String nombre, String fotoUrl, String id) {
            this.nombre = nombre;
            this.fotoUrl = fotoUrl;
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getFotoUrl() {
            return fotoUrl;
        }

        public void setFotoUrl(String fotoUrl) {
            this.fotoUrl = fotoUrl;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

}
