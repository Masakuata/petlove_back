package xatal.petlove.structures;

import xatal.petlove.entities.Producto;

import java.util.List;

public class FullVenta {
	public PublicCliente cliente;
	public String vendedor;
	public boolean pagado;
	public String fecha;
	public boolean facturado;
	public float abonado;
	public float total;
	public String direccion;
	public List<Producto> productos;

	public FullVenta() {}
}
