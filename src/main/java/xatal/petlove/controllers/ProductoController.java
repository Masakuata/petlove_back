package xatal.petlove.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xatal.petlove.entities.Producto;
import xatal.petlove.services.PrecioProductoService;
import xatal.petlove.services.ProductoService;
import xatal.petlove.services.SearchProductoService;
import xatal.petlove.structures.DetailedPrecio;
import xatal.petlove.structures.MultiDetailedPrecioProducto;
import xatal.petlove.structures.MultiPrecioProducto;
import xatal.petlove.structures.ProductoDetallesRequestBody;
import xatal.petlove.structures.PublicPrecio;

import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/producto")
public class ProductoController {
	private final ProductoService productoService;
	private final SearchProductoService searchProductoService;
	private final PrecioProductoService precioProductoService;

	public ProductoController(ProductoService productoService, SearchProductoService searchProductoService, PrecioProductoService precioProductoService) {
		this.productoService = productoService;
		this.searchProductoService = searchProductoService;
		this.precioProductoService = precioProductoService;
	}

	@GetMapping()
	public ResponseEntity<?> getProductos(
		@RequestParam(name = "id_producto", required = false, defaultValue = "") Optional<Integer> idProducto,
		@RequestParam(name = "nombre", required = false, defaultValue = "") Optional<String> nombreQuery,
		@RequestParam(name = "tipo_cliente", required = false, defaultValue = "-1") Optional<Integer> tipoCliente,
		@RequestParam(name = "size", required = false, defaultValue = "10") Integer size,
		@RequestParam(name = "pag", required = false, defaultValue = "0") Integer pag
	) {
		List<Producto> productos = this.searchProductoService.search(
			idProducto.orElse(null),
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
	public ResponseEntity<?> getFullProducto(@PathVariable("id_producto") long idProducto) {
		Optional<Producto> productoOptional = this.searchProductoService.searchProductoById(idProducto);
		if (productoOptional.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		MultiDetailedPrecioProducto detailedProducto =
			this.precioProductoService.getWithPreciosAndTipoCliente(productoOptional.get());
		return ResponseEntity.ok(detailedProducto);
	}

	@PostMapping()
	public ResponseEntity<?> registerProducto(@RequestBody MultiPrecioProducto newProducto) {
		if (this.productoService.isProductoRegistered(newProducto)) {
			return new ResponseEntity<>(HttpStatus.CONFLICT);
		}
		Producto savedProducto = this.productoService.saveProducto(newProducto);
		if (savedProducto != null) {
			return new ResponseEntity<>(savedProducto, HttpStatus.CREATED);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	@GetMapping("/precio")
	public ResponseEntity<?> getWithPrecios() {
		List<MultiPrecioProducto> productos = this.precioProductoService.getWithPrecios();
		if (productos.isEmpty()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(productos);
	}

	@PutMapping("/{id_producto}")
	public ResponseEntity<?> updateProducto(
		@PathVariable("id_producto") long idProducto,
		@RequestBody MultiPrecioProducto producto
	) {
		if (!this.productoService.isIdRegistered(idProducto)) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(this.productoService.updateProducto(producto, idProducto));
	}

	@PostMapping("/{id_producto}/precio")
	public ResponseEntity<?> setPrecios(
		@PathVariable("id_producto") int idProducto,
		@RequestBody List<PublicPrecio> precios
	) {
		if (this.precioProductoService.savePreciosById(idProducto, precios)) {
			return ResponseEntity.ok().build();
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}


	@DeleteMapping("/{id_producto}")
	public ResponseEntity<?> deleteProducto(@PathVariable("id_producto") int idProducto) {
		if (!this.productoService.isIdRegistered(idProducto)) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		if (this.productoService.isReferenced(idProducto)) {
			this.productoService.deactivateProducto(idProducto);
		} else {
			this.productoService.deleteProducto(idProducto);
		}
		return ResponseEntity.ok().build();
	}

	@PatchMapping("/detalles")
	public ResponseEntity<?> getDetallesProductos(@RequestBody ProductoDetallesRequestBody payload) {
		List<Producto> productos = this.searchProductoService.searchByIdsAndTipoCliente(payload.productos, payload.tipoCliente);
		if (productos.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(productos);
	}

	@GetMapping("/{id_producto}/precios")
	public ResponseEntity<?> getPreciosProducto(@PathVariable("id_producto") long idProducto) {
		List<DetailedPrecio> detailedPrecios = this.precioProductoService.getDetailedPrecios(idProducto);
		if (detailedPrecios == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(detailedPrecios);
	}

	@PutMapping("/{id_producto}/stock")
	public ResponseEntity<?> updateStock(
		@PathVariable("id_producto") long idProducto,
		@RequestParam(name = "cant") int stockDelta
	) {
		this.productoService.updateStockWithDelta(idProducto, stockDelta);
		return ResponseEntity.ok().build();
	}
}
