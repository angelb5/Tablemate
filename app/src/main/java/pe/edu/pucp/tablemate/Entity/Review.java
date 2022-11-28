package pe.edu.pucp.tablemate.Entity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.io.Serializable;

public class Review implements Serializable {

    @Exclude
    private String key;
    private RevUser user;
    private int rating;
    private String content;
    private String fotoUrl;
    private transient Timestamp timestamp;

    public Review() {
    }

    public Review(RevUser user, int rating, String content, String fotoUrl, Timestamp timestamp) {
        this.user = user;
        this.rating = rating;
        this.content = content;
        this.fotoUrl = fotoUrl;
        this.timestamp = timestamp;
    }

    public RevUser getUser() {
        return user;
    }

    public void setUser(RevUser user) {
        this.user = user;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String key) {
        this.key = key;
    }

    public static class RevUser implements Serializable {

        private String nombre;
        private String uid;
        private String avatarUrl;

        public RevUser() {
        }

        public RevUser(String nombre, String uid, String avatarUrl) {
            this.nombre = nombre;
            this.uid = uid;
            this.avatarUrl = avatarUrl;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }

        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }

}
