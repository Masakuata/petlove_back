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

import java.util.List;

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
        List<Cliente> clientes;
        if (nombreQuery != null && !nombreQuery.isEmpty()) {
            clientes = this.clienteService.searchByName(nombreQuery, size);
        } else {
            clientes = this.clienteService.getAll();
        }
        if (clientes.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(clientes);
    }

    @PostMapping()
    public ResponseEntity addCliente(@RequestBody Cliente cliente) {
        if (this.clienteService.isEmailUsed(cliente.getEmail())) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        cliente = this.clienteService.saveCliente(cliente);
        if (cliente == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(cliente, HttpStatus.CREATED);
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
