package xatal.sharedz.controllers;

import com.fasterxml.jackson.databind.ser.std.ToEmptyObjectSerializer;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.security.TokenUtils;
import xatal.sharedz.services.ProductoService;
import xatal.sharedz.structures.NewProducto;

import java.util.List;

@RestController
@RequestMapping("/producto")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping()
    public ResponseEntity getProductos(@RequestHeader("Token") String token) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
        List<Producto> productos = this.productoService.getAll();
        if (productos.isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(productos);
    }

    @PostMapping()
    public ResponseEntity registerProducto(
            @RequestHeader("Token") String token,
            @RequestBody NewProducto newProducto) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (!newProducto.isValid() || claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        Producto savedProducto = this.productoService.newProducto(newProducto);
        if (savedProducto != null) {
            return new ResponseEntity(savedProducto, HttpStatus.CREATED);
        }
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{id_producto}")
    public ResponseEntity deleteProducto(
            @RequestHeader("Token") String token,
            @PathVariable("id_producto") int idProducto) {
        Claims claims = TokenUtils.getTokenClaims(token);
        if (claims == null || TokenUtils.isExpired(claims)) {
            return new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
        }
        if (!this.productoService.isIdRegistered(idProducto)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        this.productoService.deleteById(idProducto);
        return ResponseEntity.ok().build();
    }
}
