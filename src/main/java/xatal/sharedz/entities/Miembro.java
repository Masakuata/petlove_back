package xatal.sharedz.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import xatal.sharedz.security.TokenUtils;
import xatal.sharedz.structures.Login;

@Entity
public class Miembro {
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
        this.password = new BCryptPasswordEncoder().encode(this.password);
    }

    public String getToken() {
        return TokenUtils.createToken(this.username, this.email);
    }

    public static boolean isValid(Miembro miembro) {
        return miembro.username != null && !miembro.username.isEmpty()
                && miembro.email != null && !miembro.email.isEmpty()
                && miembro.password != null && !miembro.password.isEmpty();
    }

    public static Miembro fromLogin(Login login) {
        Miembro miembro = new Miembro();
        miembro.email = login.email;
        miembro.password = login.password;
        return miembro;
    }
}
