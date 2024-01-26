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
import xatal.petlove.entities.Direccion;
import xatal.petlove.services.ClienteService;
import xatal.petlove.structures.NewCliente;
import xatal.petlove.structures.PublicCliente;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
	public ResponseEntity<?> getClientes(
		@RequestParam(name = "id_cliente", required = false, defaultValue = "") Optional<Integer> idCliente,
		@RequestParam(name = "nombre", required = false, defaultValue = "") String nombreQuery,
		@RequestParam(name = "cant", required = false, defaultValue = "10") int size,
		@RequestParam(name = "pag", required = false, defaultValue = "0") int pag
	) {
		List<PublicCliente> clientes = this.clienteService.toPublicCliente(this.clienteService.search(
			idCliente.orElse(null),
			nombreQuery,
			size,
			pag
		));
		if (clientes.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(clientes);
	}

	@GetMapping("/all")
	public ResponseEntity<?> getAll() {
		List<PublicCliente> clientes = this.clienteService.getAllPublic();
		if (clientes.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(clientes);
	}

	@PostMapping()
	public ResponseEntity<?> addCliente(@RequestBody NewCliente cliente) {
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
		return ResponseEntity.ok(new PublicCliente(cliente));
	}

	@PutMapping("/{cliente_id}")
	public ResponseEntity<?> updateCliente(
		@PathVariable("cliente_id") int clienteId,
		@RequestBody PublicCliente cliente
	) {
		if (!this.clienteService.isIdRegistered(clienteId)) {
			return ResponseEntity.notFound().build();
		}
		cliente.id = clienteId;
		Cliente savedCliente = this.clienteService.updateCliente(cliente);
		if (savedCliente == null) {
			return ResponseEntity.internalServerError().build();
		}
		return ResponseEntity.ok(cliente);
	}

	@DeleteMapping("/{cliente_id}")
	public ResponseEntity<?> deleteCliente(@PathVariable("cliente_id") int clienteId) {
		if (!this.clienteService.isIdRegistered(clienteId)) {
			return ResponseEntity.notFound().build();
		}
		this.clienteService.removeById(clienteId);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{cliente_id}/direccion")
	public ResponseEntity<?> addDireccion(
		@PathVariable("cliente_id") Long idCliente,
		@RequestBody Map<String, String> direccion
	) {
		Optional<Direccion> optionalDireccion = this.clienteService.addDireccion(idCliente, direccion.get("direccion"));
		return optionalDireccion
			.map(value ->
				new ResponseEntity(value, HttpStatus.CREATED))
			.orElseGet(() -> new ResponseEntity<>(HttpStatus.CONFLICT));
	}

	@PutMapping("/{cliente_id}/direccion/{direccion_id}")
	public ResponseEntity<?> updateDireccion(
		@PathVariable("cliente_id") Long idCliente,
		@PathVariable("direccion_id") Long idDireccion,
		@RequestBody Map<String, String> direccion
	) {
		if (!this.clienteService.updateDireccion(idCliente, idDireccion, direccion.get("direccion"))) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{cliente_id}/direccion/{direccion_id}")
	public ResponseEntity<?> deleteDireccion(
		@PathVariable("cliente_id") Long idCliente,
		@PathVariable("direccion_id") Long idDireccion
	) {
		if (!this.clienteService.isDireccionRegistered(idDireccion)) {
			return ResponseEntity.notFound().build();
		}
		if (this.clienteService.isDireccionReferenced(idDireccion)) {
			this.clienteService.deactivateDireccion(idDireccion);
		}
		this.clienteService.deleteDireccion(idCliente, idDireccion);
		return ResponseEntity.ok().build();
	}
}
