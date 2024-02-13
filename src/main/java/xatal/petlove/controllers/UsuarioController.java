package xatal.petlove.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.entities.Usuario;
import xatal.petlove.services.UsuarioService;
import xatal.petlove.structures.Login;
import xatal.petlove.structures.PublicUsuario;
import xatal.petlove.util.TokenUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin
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
        if (!this.usuarioService.isUserAvailable(usuario)) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        usuario.encodePassword();
        usuario = this.usuarioService.saveUsuario(usuario);
        if (usuario != null) {
            HashMap<String, String> response = new HashMap<>();
            response.put("email", usuario.getEmail());
            response.put("username", usuario.getUsername());
            response.put("token", usuario.getToken());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
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
            HashMap<String, String> response = new HashMap<>();
            response.put("username", miembro.getUsername());
            response.put("email", miembro.getEmail());
            response.put("token", miembro.getToken());
            return new ResponseEntity(response, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/token")
    public ResponseEntity checkToken(@RequestHeader("Token") String token) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (!this.usuarioService.isEmailUsed(claims.getSubject())) {
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
        if (!this.usuarioService.deleteUsuario(claims.getSubject())) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{username}/username")
    public ResponseEntity changeUsername(
            @RequestHeader("Token") String token,
            @RequestBody Map<String, String> payload
    ) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (!this.usuarioService.isUsernameUsed(payload.get("username"))) {
            Optional<Usuario> optionalUsuario = this.usuarioService.getUsuarioFromEmail(claims.getSubject());
            if (optionalUsuario.isEmpty()) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            Usuario usuario = optionalUsuario.get();
            usuario.setUsername(payload.get("username"));
            usuario = this.usuarioService.saveUsuario(usuario);
            return ResponseEntity.ok(new PublicUsuario(usuario));
        }
        return new ResponseEntity(HttpStatus.CONFLICT);
    }

    @PutMapping("/{username}/email")
    public ResponseEntity changeEmail(
            @RequestHeader("Token") String token,
            @RequestBody Map<String, String> payload) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (!this.usuarioService.isEmailUsed(payload.get("email"))) {
            Optional<Usuario> optionalUsuario = this.usuarioService.getUsuarioFromEmail(claims.getSubject());
            if (optionalUsuario.isEmpty()) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            Usuario usuario = optionalUsuario.get();
            usuario.setEmail(payload.get("email"));
            usuario = this.usuarioService.saveUsuario(usuario);
            return ResponseEntity.ok(new PublicUsuario(usuario));
        }
        return new ResponseEntity(HttpStatus.CONFLICT);
    }

    @PutMapping("/{username}/password")
    public ResponseEntity changePassword(
            @RequestHeader("Token") String token,
            @RequestBody Map<String, String> payload) {
        Claims claims = TokenUtils.getTokenClaims(token);
        Optional<Usuario> optionalUsuario = this.usuarioService.getUsuarioFromEmail(claims.getSubject());
        if (optionalUsuario.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        Usuario usuario = optionalUsuario.get();
        usuario.setPassword(payload.get("password"));
        usuario.encodePassword();
        usuario = this.usuarioService.saveUsuario(usuario);
        return ResponseEntity.ok(new PublicUsuario(usuario));
    }
}
