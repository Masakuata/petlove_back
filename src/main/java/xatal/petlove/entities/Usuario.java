package xatal.petlove.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import xatal.petlove.structures.Login;
import xatal.petlove.util.TokenUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Entity
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT")
    private boolean status = true;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public boolean validLogin() {
        return this.email != null && !this.email.isEmpty()
                && this.password != null && !this.password.isEmpty();
    }

    public void encodePassword() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            this.password = Base64.getEncoder().encodeToString(digest.digest(this.password.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getToken() {
        return TokenUtils.createToken(this.username, this.email, this.id);
    }

    public static boolean isValid(Usuario miembro) {
        return miembro.username != null && !miembro.username.isEmpty()
                && miembro.email != null && !miembro.email.isEmpty()
                && miembro.password != null && !miembro.password.isEmpty();
    }

    public static Usuario fromLogin(Login login) {
        Usuario miembro = new Usuario();
        miembro.email = login.email;
        miembro.password = login.password;
        return miembro;
    }
}
