package xatal.sharedz.controllers;

import org.springframework.format.annotation.DateTimeFormat;
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
import xatal.sharedz.entities.Venta;
import xatal.sharedz.services.VentaService;
import xatal.sharedz.structures.PublicVenta;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@CrossOrigin
@RestController
@RequestMapping("/venta")
public class VentaController {
    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    public ResponseEntity getVentas(
            @RequestParam(name = "cliente", required = false) Optional<String> clienteNombre,
            @RequestParam(name = "fecha", required = false) @DateTimeFormat(pattern = "dd-MM-yyyy") Optional<Date> fecha
    ) {
        List<Venta> ventas = this.ventaService.searchVentas(
                clienteNombre.orElse(null),
                fecha.orElse(null));
        if (ventas.isEmpty()) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(this.ventaService.fromVentas(ventas));
    }

    @PostMapping
    public ResponseEntity newVenta(@RequestBody PublicVenta venta) {
        Venta savedVenta = this.ventaService.newVenta(venta);
        if (savedVenta == null) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity(new PublicVenta(savedVenta), HttpStatus.CREATED);
    }

    @GetMapping("/{id_venta}")
    public ResponseEntity getVenta(@PathVariable("id_venta") int ventaId) {
        Venta venta = this.ventaService.getById(ventaId);
        if (venta == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(venta);
    }

    @PutMapping("/{id_venta}")
    public ResponseEntity updateVenta(
            @PathVariable("id_venta") int ventaId,
            @RequestBody PublicVenta venta
    ) {
        venta.id = ventaId;
        Venta updatedVenta = this.ventaService.updateVenta(venta);
        if (updatedVenta == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(updatedVenta);
    }

    @DeleteMapping("/{id_venta}")
    public ResponseEntity deleteVenta(@PathVariable("id_venta") int ventaId) {
        if (!this.ventaService.isIdRegistered(ventaId)) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
        this.ventaService.deleteById(ventaId);
        return new ResponseEntity(HttpStatus.OK);
    }
}
