package xatal.petlove.structures;

import xatal.petlove.entities.ProductoVenta;

public class PublicProductoVenta {
    public Long producto = -1L;
    public int cantidad = 0;

    public PublicProductoVenta() {
    }

    public PublicProductoVenta(ProductoVenta venta) {
        this.producto = venta.getProducto();
        this.cantidad = venta.getCantidad();
    }

    public PublicProductoVenta(Long producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }
}
