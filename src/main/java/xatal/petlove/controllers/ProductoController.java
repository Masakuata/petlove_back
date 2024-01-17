package xatal.petlove.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xatal.petlove.entities.Producto;
import xatal.petlove.services.ProductoService;
import xatal.petlove.structures.MultiPrecioProducto;
import xatal.petlove.structures.ProductoDetallesRequestBody;
import xatal.petlove.structures.ProductoLoad;
import xatal.petlove.structures.PublicPrecio;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/producto")
public class ProductoController {
	private final ProductoService productoService;

	public ProductoController(ProductoService productoService) {
		this.productoService = productoService;
	}

	@PostMapping("/cargar")
	public ResponseEntity cargarProductos(@RequestBody List<ProductoLoad> productos) {
		this.productoService.cargarProductos(productos);
		return ResponseEntity.ok().build();
	}

	@GetMapping()
	public ResponseEntity getProductos(
		@RequestParam(name = "nombre", required = false, defaultValue = "") Optional<String> nombreQuery,
		@RequestParam(name = "tipo_cliente", required = false, defaultValue = "-1") Optional<Integer> tipoCliente,
		@RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
		@RequestParam(name = "pag", required = false, defaultValue = "0") Integer pag
	) {
		List<Producto> productos = this.productoService.search(
			nombreQuery.orElse(null),
			tipoCliente.orElse(null),
			size,
			pag
		);
		if (productos.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(productos);
	}

	@GetMapping("/{id_producto}")
	public ResponseEntity getFullProducto(@PathVariable("id_producto") long idProducto) {
		Optional<MultiPrecioProducto> optionalProducto = this.productoService.getWithPreciosById(idProducto);
		if (optionalProducto.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(optionalProducto.get());
	}

	@PostMapping()
	public ResponseEntity registerProducto(@RequestBody MultiPrecioProducto newProducto) {
		if (this.productoService.isProductoRegistered(newProducto)) {
			return new ResponseEntity(HttpStatus.CONFLICT);
		}
		Producto savedProducto = this.productoService.saveProducto(newProducto);
		if (savedProducto != null) {
			return new ResponseEntity(savedProducto, HttpStatus.CREATED);
		}
		return new ResponseEntity(HttpStatus.BAD_REQUEST);
	}

	@GetMapping("/precio")
	public ResponseEntity getWithPrecios() {
		List<MultiPrecioProducto> productos = this.productoService.getWithPrecios();
    	if (productos.isEmpty()) {
    		return ResponseEntity.noContent().build();
    	}
    	return ResponseEntity.ok(productos);
	}

	@PutMapping("/{id_producto}")
	public ResponseEntity updateProducto(
		@PathVariable("id_producto") long idProducto,
		@RequestBody Producto producto
	) {
		if (!this.productoService.isIdRegistered(idProducto)) {
			return ResponseEntity.notFound().build();
		}
		producto.setId(idProducto);
		return ResponseEntity.ok(this.productoService.saveProducto(producto));
	}

	@PostMapping("/{id_producto}/precio")
	public ResponseEntity setPrecios(
		@PathVariable("id_producto") int idProducto,
		@RequestBody List<PublicPrecio> precios
	) {
		if (this.productoService.savePreciosById(idProducto, precios)) {
			return ResponseEntity.ok().build();
		}
		return new ResponseEntity(HttpStatus.NOT_FOUND);
	}


	@DeleteMapping("/{id_producto}")
	public ResponseEntity deleteProducto(@PathVariable("id_producto") int idProducto) {
		if (!this.productoService.isIdRegistered(idProducto)) {
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		}
		if (this.productoService.isReferenced(idProducto)) {
			return new ResponseEntity(HttpStatus.CONFLICT);
		}
		this.productoService.deleteById(idProducto);
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/detalles")
	public ResponseEntity getDetallesProductos(@RequestBody ProductoDetallesRequestBody payload) {
		List<Producto> productos = this.productoService.searchByIdsAndTipoCliente(payload.productos, payload.tipoCliente);
		if (productos.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(productos);
	}
}
