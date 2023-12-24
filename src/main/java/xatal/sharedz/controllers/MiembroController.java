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
import xatal.sharedz.entities.Miembro;
import xatal.sharedz.security.TokenUtils;
import xatal.sharedz.services.MiembroService;
import xatal.sharedz.structures.Login;
import xatal.sharedz.structures.PublicMiembro;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/miembro")
public class MiembroController {
    private final MiembroService miembroService;

    public MiembroController(MiembroService miembroService) {
        this.miembroService = miembroService;
    }

    @GetMapping()
    public ResponseEntity getMiembros() {
        List<PublicMiembro> miembros = this.miembroService.getAllPublic();
        if (miembros.isEmpty()) {
            return new ResponseEntity<HttpStatus>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(miembros);
    }

    @PostMapping()
    public ResponseEntity addMiembro(@RequestBody Miembro miembro) {
        if (!Miembro.isValid(miembro)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (this.miembroService.areCredentialsUsed(miembro.getEmail(), miembro.getUsername())) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        miembro.encodePassword();
        miembro = this.miembroService.saveMiembro(miembro);
        if (miembro != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Token", miembro.getToken());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .headers(headers)
                    .body(new PublicMiembro(miembro));
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody Login login) {
        Miembro miembro = Miembro.fromLogin(login);
        if (!miembro.validLogin()) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        miembro.encodePassword();
        miembro = this.miembroService.login(miembro);
        if (miembro != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Token", miembro.getToken());
            return new ResponseEntity(new PublicMiembro(miembro), headers, HttpStatus.OK);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

//    @PostMapping("/token")
//    public ResponseEntity checkToken()

    @DeleteMapping()
    public ResponseEntity delete(@RequestHeader("Token") String token) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.miembroService.deleteMiembro(claims.getSubject())) {
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
        if (!this.miembroService.isUsernameUsed(payload.get("username"))) {
            Miembro miembro = this.miembroService.getMiembroFromEmail(claims.getSubject());
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            miembro.setUsername(payload.get("username"));
            miembro = this.miembroService.saveMiembro(miembro);
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(new PublicMiembro(miembro));
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
        if (!this.miembroService.isEmailUsed(payload.get("email"))) {
            Miembro miembro = this.miembroService.getMiembroFromEmail(claims.getSubject());
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.NOT_FOUND);
            }
            miembro.setEmail(payload.get("email"));
            miembro = this.miembroService.saveMiembro(miembro);
            if (miembro == null) {
                return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(new PublicMiembro(miembro));
        }
        return new ResponseEntity(HttpStatus.CONFLICT);
    }
}
