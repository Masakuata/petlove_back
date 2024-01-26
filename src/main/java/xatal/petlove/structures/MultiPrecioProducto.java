package xatal.petlove.structures;

import xatal.petlove.entities.Producto;

import java.util.LinkedList;
import java.util.List;

public class MultiPrecioProducto extends PublicProducto {
	public List<PublicPrecio> precios = new LinkedList<>();

	public MultiPrecioProducto() {
	}

	public MultiPrecioProducto(Producto producto) {
		this.nombre = producto.getNombre();
		this.presentacion = producto.getPresentacion();
		this.tipoMascota = producto.getTipoMascota();
		this.raza = producto.getRaza();
		this.precio = producto.getPrecio();
		this.cantidad = producto.getCantidad();
		this.peso = producto.getPeso();
	}
}
