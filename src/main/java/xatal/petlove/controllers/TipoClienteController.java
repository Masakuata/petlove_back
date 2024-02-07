package xatal.petlove.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.services.TipoClienteService;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/tipoCliente")
public class TipoClienteController {
	private final TipoClienteService tipoClienteService;

	public TipoClienteController(TipoClienteService tipoClienteService) {
		this.tipoClienteService = tipoClienteService;
	}

	@GetMapping
	public ResponseEntity<?> getTiposCliente() {
		return ResponseEntity.ok(this.tipoClienteService.getTiposCliente());
	}

	@PostMapping
	public ResponseEntity<?> newTipoCliente(@RequestBody Map<String, String> nombres) {
		if (nombres.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		return ResponseEntity.ok(this.tipoClienteService.newTipoCliente(nombres.get("nombre")));
	}
}
