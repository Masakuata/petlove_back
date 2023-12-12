package xatal.sharedz.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.sharedz.entities.Miembro;
import xatal.sharedz.services.MiembroService;
import xatal.sharedz.structures.Login;
import xatal.sharedz.structures.PublicMiembro;

import java.util.List;

@RestController
@RequestMapping("/miembro")
public class MiembroController {
    Logger logger = LoggerFactory.getLogger(MiembroController.class);
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
            return ResponseEntity.ok()
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
}