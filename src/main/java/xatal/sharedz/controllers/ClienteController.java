package xatal.sharedz.controllers;

import io.jsonwebtoken.Claims;
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
import xatal.sharedz.entities.Cliente;
import xatal.sharedz.security.TokenUtils;
import xatal.sharedz.services.ClienteService;

import java.util.List;

@RestController
@RequestMapping("/cliente")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping()
    public ResponseEntity getClientes(@RequestHeader("Token") String token) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        List<Cliente> clientes = this.clienteService.getAll();
        if (clientes.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(clientes);
    }

    @PostMapping()
    public ResponseEntity addCliente(
            @RequestHeader("Token") String token,
            @RequestBody Cliente cliente) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (this.clienteService.isEmailUsed(cliente.getEmail())) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        cliente = this.clienteService.saveCliente(cliente);
        if (cliente == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{cliente_id}")
    public ResponseEntity updateCliente(
            @RequestHeader("Token") String token,
            @PathVariable("cliente_id") int clienteId,
            @RequestBody Cliente cliente
    ) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.clienteService.isIdRegistered(clienteId)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        cliente.setId((long) clienteId);
        cliente = this.clienteService.saveCliente(cliente);
        if (cliente == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping("/{cliente_id}")
    public ResponseEntity deleteCliente(
            @RequestHeader("Token") String token,
            @PathVariable("cliente_id") int clienteId) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.clienteService.isIdRegistered(clienteId)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        this.clienteService.removeById(clienteId);
        return ResponseEntity.ok().build();
    }
}
