package xatal.petlove.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.services.TipoClienteService;

@CrossOrigin
@RestController
@RequestMapping("/tipoCliente")
public class TipoClienteController {
	private final TipoClienteService tipoClienteService;

	public TipoClienteController(TipoClienteService tipoClienteService) {
		this.tipoClienteService = tipoClienteService;
	}

	@GetMapping
	public ResponseEntity getTiposCliente() {
		return ResponseEntity.ok(this.tipoClienteService.getTiposCliente());
	}


}
