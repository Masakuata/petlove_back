package xatal.sharedz.structures;

import xatal.sharedz.entities.ProductoVenta;

public class PublicProductoVenta {
    public int producto = -1;
    public int cantidad = 0;

    public PublicProductoVenta() {
    }

    public PublicProductoVenta(ProductoVenta venta) {
        this.producto = Math.toIntExact(venta.getProducto());
        this.cantidad = venta.getCantidad();
    }
}
