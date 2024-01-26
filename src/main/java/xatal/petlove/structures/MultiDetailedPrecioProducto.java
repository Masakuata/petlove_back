package xatal.petlove.structures;

import xatal.petlove.entities.Producto;

import java.util.LinkedList;
import java.util.List;

public class MultiDetailedPrecioProducto extends PublicProducto {
	public List<DetailedPrecio> precios = new LinkedList<>();

	public MultiDetailedPrecioProducto() {
	}

	public MultiDetailedPrecioProducto(Producto producto) {
		this.nombre = producto.getNombre();
		this.presentacion = producto.getPresentacion();
		this.tipoMascota = producto.getTipoMascota();
		this.raza = producto.getRaza();
		this.precio = producto.getPrecio();
		this.cantidad = producto.getCantidad();
		this.peso = producto.getPeso();
	}

	public MultiDetailedPrecioProducto(MultiPrecioProducto producto) {
		this.nombre = producto.nombre;
		this.presentacion = producto.presentacion;
		this.tipoMascota = producto.tipoMascota;
		this.raza = producto.raza;
		this.precio = producto.precio;
		this.cantidad = producto.cantidad;
		this.peso = producto.peso;
	}
}
