package pe.edu.pucp.tablemate.Entity;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;

public class Restaurant {
    @Exclude
    private String key;
    @Exclude
    private double distance;
    private String nombre;
    private String categoria;
    private String descripcion;
    private String cartaUrl;
    private GeoPoint geoPoint;
    private String direccion;
    private List<String> fotosUrl;
    private List<String> searchKeywords;
    private double rating;
    private int numReviews;

    public Restaurant() {
    }

    public Restaurant(String nombre, String categoria, String descripcion, String cartaUrl, GeoPoint geoPoint, String direccion, List<String> fotosUrl, List<String> searchKeywords) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cartaUrl = cartaUrl;
        this.geoPoint = geoPoint;
        this.direccion = direccion;
        this.fotosUrl = fotosUrl;
        this.searchKeywords = searchKeywords;
        rating = 0;
        numReviews = 0;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCartaUrl() {
        return cartaUrl;
    }

    public void setCartaUrl(String cartaUrl) {
        this.cartaUrl = cartaUrl;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<String> getFotosUrl() {
        return fotosUrl;
    }

    public void setFotosUrl(List<String> fotosUrl) {
        this.fotosUrl = fotosUrl;
    }

    public List<String> getSearchKeywords() {
        return searchKeywords;
    }

    public void setSearchKeywords(List<String> searchKeywords) {
        this.searchKeywords = searchKeywords;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public void setNumReviews(int numReviews) {
        this.numReviews = numReviews;
    }

    @Exclude
    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
