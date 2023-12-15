package xatal.sharedz.controllers;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.sharedz.entities.Grupo;
import xatal.sharedz.entities.Miembro;
import xatal.sharedz.security.TokenUtils;
import xatal.sharedz.services.GrupoService;
import xatal.sharedz.services.MiembroService;
import xatal.sharedz.structures.PublicGrupo;

@RestController
@CrossOrigin
@RequestMapping("/grupo")
public class GrupoController {
    private final GrupoService grupoService;
    private final MiembroService miembroService;

    public GrupoController(GrupoService grupoService, MiembroService miembroService) {
        this.grupoService = grupoService;
        this.miembroService = miembroService;
    }

    @PostMapping("/{grupoName}")
    public ResponseEntity addGrupo(
            @RequestHeader("Token") String token,
            @PathVariable(name = "grupoName") String grupoName
    ) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (this.grupoService.isNameUsed(grupoName)) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        Grupo newGrupo = this.grupoService.newGrupo(
                grupoName,
                this.miembroService.getMiembroFromEmail(claims.getSubject()));
        if (newGrupo != null) {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new PublicGrupo(newGrupo));
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{grupoName}/{username}")
    public ResponseEntity removeMember(
            @RequestHeader("Token") String token,
            @PathVariable(name = "grupoName") String grupoName,
            @PathVariable(name = "username") String username
    ) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || !claims.get("username").equals(username)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        Miembro miembro = this.miembroService.getMiembroFromEmail(claims.getSubject());
        if (miembro == null || !this.grupoService.isMemberIn(grupoName, miembro)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        this.grupoService.removeMiembro(grupoName, miembro);
        return ResponseEntity.ok().build();
    }
}
