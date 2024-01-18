package xatal.petlove.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.services.ProductoService;

@CrossOrigin
@RestController
@RequestMapping("/tipoCliente")
public class TipoClienteController {
	private final ProductoService productoService;

	public TipoClienteController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@GetMapping
	public ResponseEntity getTiposCliente() {
		return ResponseEntity.ok(this.productoService.getTiposCliente());
	}
}
