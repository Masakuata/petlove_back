package xatal.sharedz.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.services.ProductoService;
import xatal.sharedz.structures.PublicPrecio;
import xatal.sharedz.structures.PublicProducto;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/producto")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping()
    public ResponseEntity getProductos(
            @RequestParam(name = "nombre", required = false, defaultValue = "") String nombreQuery,
            @RequestParam(name = "tipo_cliente", required = false, defaultValue = "-1") int tipoCliente
    ) {
        List<Producto> productos;
        if (nombreQuery != null && !nombreQuery.isEmpty()) {
            if (tipoCliente != -1) {
                productos = this.productoService.searchByNameAndTipoCliente(nombreQuery, tipoCliente);
            } else {
                productos = this.productoService.searchByName(nombreQuery);
            }
        } else {
            productos = this.productoService.getAll();
        }
        if (productos.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(productos);
    }

    @PostMapping()
    public ResponseEntity registerProducto(@RequestBody PublicProducto newProducto) {
        Producto savedProducto = this.productoService.newProducto(newProducto);
        if (savedProducto != null) {
            return new ResponseEntity(savedProducto, HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/{id_producto}/precio")
    public ResponseEntity setPrecios(
            @PathVariable("id_producto") int idProducto,
            @RequestBody List<PublicPrecio> precios
    ) {
        if (this.productoService.setPreciosById(idProducto, precios)) {
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
}
