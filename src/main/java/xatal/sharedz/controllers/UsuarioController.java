package xatal.sharedz.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.sharedz.entities.Usuario;
import xatal.sharedz.security.TokenUtils;
import xatal.sharedz.services.UsuarioService;
import xatal.sharedz.structures.Login;
import xatal.sharedz.structures.PublicUsuario;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuario")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService miembroService) {
        this.usuarioService = miembroService;
    }

    @GetMapping()
    public ResponseEntity getUsuarios() {
        List<PublicUsuario> usuarios = this.usuarioService.getAllPublic();
        if (usuarios.isEmpty()) {
            return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping()
    public ResponseEntity addUsuario(@RequestBody Usuario usuario) {
        if (!Usuario.isValid(usuario)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (this.usuarioService.areCredentialsUsed(usuario.getEmail(), usuario.getUsername())) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        usuario.encodePassword();
        usuario = this.usuarioService.saveUsuario(usuario);
        if (usuario != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Token", usuario.getToken());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(new PublicUsuario(usuario));
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Login login) {
        Usuario miembro = Usuario.fromLogin(login);
        if (!miembro.validLogin()) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        miembro.encodePassword();
        miembro = this.usuarioService.login(miembro);
        if (miembro != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Token", miembro.getToken());
            return new ResponseEntity(new PublicUsuario(miembro), headers, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/token")
    public ResponseEntity checkToken(@RequestHeader("Token") String token) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.usuarioService.areCredentialsUsed(claims.getSubject(), claims.get("username").toString())) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        PublicUsuario miembro = new PublicUsuario();
        miembro.email = claims.getSubject();
        miembro.username = claims.get("username").toString();
        String newToken = TokenUtils.createToken(claims);
        HttpHeaders headers = new HttpHeaders();
        headers.add("Token", newToken);
        return new ResponseEntity(miembro, headers, HttpStatus.OK);
    }

    @DeleteMapping()
    public ResponseEntity delete(@RequestHeader("Token") String token) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.usuarioService.deleteUsuario(claims.getSubject())) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/username")
    public ResponseEntity changeUsername(
            @RequestHeader("Token") String token,
            @PathVariable("username") String username,
            @RequestBody Map<String, String> payload) {

        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || !claims.get("username").equals(username)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.usuarioService.isUsernameUsed(payload.get("username"))) {
            Usuario miembro = this.usuarioService.getUsuarioFromEmail(claims.getSubject());
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            miembro.setUsername(payload.get("username"));
            miembro = this.usuarioService.saveUsuario(miembro);
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(new PublicUsuario(miembro));
        }
        return new ResponseEntity(HttpStatus.CONFLICT);
    }

    @PutMapping("/{username}/email")
    public ResponseEntity changeEmail(
            @RequestHeader("Token") String token,
            @PathVariable("username") String username,
            @RequestBody Map<String, String> payload) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || !claims.get("username").equals(username)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.usuarioService.isEmailUsed(payload.get("email"))) {
            Usuario miembro = this.usuarioService.getUsuarioFromEmail(claims.getSubject());
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            miembro.setEmail(payload.get("email"));
            miembro = this.usuarioService.saveUsuario(miembro);
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(new PublicUsuario(miembro));
        }
        return new ResponseEntity(HttpStatus.CONFLICT);
    }
}
