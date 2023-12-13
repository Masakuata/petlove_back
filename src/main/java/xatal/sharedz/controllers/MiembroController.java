package xatal.sharedz.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
            return new ResponseEntity<HttpStatus>(HttpStatus.NOT_ACCEPTABLE);
        }
        miembro.encodePassword();
        miembro = this.miembroService.addMiembro(miembro);
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
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new PublicMiembro(miembro));
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping()
    public ResponseEntity delete(@RequestBody Login login, @RequestHeader("Token") String token) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || !claims.getSubject().equals(login.email)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        Miembro miembro = Miembro.fromLogin(login);
        if (!this.miembroService.deleteMiembro(miembro)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().build();
    }
}
