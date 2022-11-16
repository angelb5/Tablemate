package pe.edu.pucp.tablemate.Entity;

public class User {
    private String nombre;
    private String apellidos;
    private String correo;
    private String dni;
    private String permisos;
    private String avatarUrl;

    public User() {
    }

    public User(String nombre, String apellidos, String correo, String dni, String permisos, String avatarUrl) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.correo = correo;
        this.dni = dni;
        this.permisos = permisos;
        this.avatarUrl = avatarUrl;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getPermisos() {
        return permisos;
    }

    public void setPermisos(String persmisos) {
        this.permisos = persmisos;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }
}
