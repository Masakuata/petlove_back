package xatal.sharedz.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xatal.sharedz.entities.Cliente;
import xatal.sharedz.services.ClienteService;
import xatal.sharedz.structures.ClienteMinimal;
import xatal.sharedz.structures.PublicCliente;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@CrossOrigin
@RestController
@RequestMapping("/cliente")
public class ClienteController {
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping()
    public ResponseEntity getClientes(
            @RequestParam(name = "nombre", required = false, defaultValue = "") String nombreQuery,
            @RequestParam(name = "cant", required = false, defaultValue = "10") int size
    ) {
        if (nombreQuery != null && !nombreQuery.isEmpty()) {
            List<PublicCliente> clientes = this.clienteService.searchByNamePublic(nombreQuery, size);
            if (clientes.isEmpty()) {
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(clientes);
        } else {
            List<ClienteMinimal> clientes = this.clienteService.getMinimal();
            if (clientes.isEmpty()) {
                return new ResponseEntity(HttpStatus.NO_CONTENT);
            }
            return ResponseEntity.ok(clientes);
        }
    }

    @PostMapping()
    public ResponseEntity addCliente(@RequestBody PublicCliente cliente) {
        if (this.clienteService.isEmailUsed(cliente.email)) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity(this.clienteService.saveCliente(cliente), HttpStatus.CREATED);
    }

    @PutMapping("/{cliente_id}")
    public ResponseEntity updateCliente(
            @PathVariable("cliente_id") int clienteId,
            @RequestBody Cliente cliente
    ) {
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
    public ResponseEntity deleteCliente(@PathVariable("cliente_id") int clienteId) {
        if (!this.clienteService.isIdRegistered(clienteId)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        this.clienteService.removeById(clienteId);
        return ResponseEntity.ok().build();
    }
}
