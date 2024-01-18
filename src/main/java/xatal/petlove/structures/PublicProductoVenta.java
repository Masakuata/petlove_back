package xatal.petlove.structures;

import xatal.petlove.entities.ProductoVenta;

public class PublicProductoVenta {
	public Long producto = -1L;
	public int cantidad = 0;
	public float precio = 0F;

	public PublicProductoVenta() {
	}

	public PublicProductoVenta(ProductoVenta venta) {
		this.producto = venta.getProducto();
		this.cantidad = venta.getCantidad();
		this.precio = venta.getPrecio();
	}

	public PublicProductoVenta(Long producto, int cantidad, float precio) {
		this.producto = producto;
		this.cantidad = cantidad;
		this.precio = precio;
	}
}
