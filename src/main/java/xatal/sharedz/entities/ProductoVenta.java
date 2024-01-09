package xatal.sharedz.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import xatal.sharedz.structures.PublicProductoVenta;

@Entity
public class ProductoVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "producto", nullable = false)
    private Long producto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    public ProductoVenta() {
    }

    public ProductoVenta(PublicProductoVenta productoVenta) {
        this.producto = productoVenta.producto;
        this.cantidad = productoVenta.cantidad;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getProducto() {
        return producto;
    }

    public void setProducto(Long producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
