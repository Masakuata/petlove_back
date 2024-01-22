package xatal.petlove.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import xatal.petlove.entities.Cliente;
import xatal.petlove.services.ClienteService;
import xatal.petlove.structures.ClienteMinimal;
import xatal.petlove.structures.PublicCliente;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;

@CrossOrigin
@RestController
@RequestMapping("/cliente")
public class ClienteController {
    private final Logger logger = LoggerFactory.getLogger(ClienteController.class);
    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping()
    public ResponseEntity getClientes(
            @RequestParam(name = "nombre", required = false, defaultValue = "") String nombreQuery,
            @RequestParam(name = "cant", required = false, defaultValue = "10") int size
    ) {
        return Optional.of(nombreQuery)
                .filter(nombre -> !nombre.isEmpty())
                .map(n -> getClientes(nombreQuery, size, this.clienteService::searchByNamePublic))
                .orElse(getMinimalClientes(size, this.clienteService::getMinimal));
    }

    @GetMapping("/all")
    public ResponseEntity getAll() {
        List<PublicCliente> clientes = this.clienteService.getAllPublic();
        if (clientes.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(clientes);
    }

    private <T> ResponseEntity getClientes(String name, int size, BiFunction<String, Integer, List<T>> fetcher) {
        return Optional.of(fetcher.apply(name, size))
                .filter(clients -> !clients.isEmpty())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    private <T> ResponseEntity getMinimalClientes(int size, Callable<List<ClienteMinimal>> fetcher) {
        try {
            List<ClienteMinimal> clientes = fetcher.call()
                    .stream()
                    .limit(size)
                    .toList();
            if (clientes.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(clientes);
        } catch (Exception e) {
            this.logger.error(e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping()
    public ResponseEntity addCliente(@RequestBody PublicCliente cliente) {
        if (this.clienteService.isEmailUsed(cliente.email)) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        return new ResponseEntity(this.clienteService.saveCliente(cliente), HttpStatus.CREATED);
    }


    @GetMapping("/{cliente_id}")
    public ResponseEntity<?> getCliente(@PathVariable("cliente_id") long idCliente) {
        Cliente cliente = this.clienteService.getById(idCliente);
        if (cliente == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @PutMapping("/{cliente_id}")
    public ResponseEntity updateCliente(
            @PathVariable("cliente_id") int clienteId,
            @RequestBody Cliente cliente
    ) {
        if (!this.clienteService.isIdRegistered(clienteId)) {
            return ResponseEntity.notFound().build();
        }
        cliente.setId((long) clienteId);
        cliente = this.clienteService.saveCliente(cliente);
        if (cliente == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(cliente);
    }

    @DeleteMapping("/{cliente_id}")
    public ResponseEntity deleteCliente(@PathVariable("cliente_id") int clienteId) {
        if (!this.clienteService.isIdRegistered(clienteId)) {
            return ResponseEntity.notFound().build();
        }
        this.clienteService.removeById(clienteId);
        return ResponseEntity.ok().build();
    }
}
