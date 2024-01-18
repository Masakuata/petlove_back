package xatal.petlove.structures;

import xatal.petlove.entities.Producto;
import xatal.petlove.entities.Venta;
import xatal.petlove.util.Util;

import java.util.List;

public class FullVenta {
	public PublicCliente cliente;
	public String vendedor;
	public boolean pagado;
	public String fecha;
	public boolean facturado;
	public float abonado;
	public float total;
	public List<Producto> productos;

	public FullVenta() {}

	public FullVenta(Venta venta) {
		this.cliente = new PublicCliente(venta.getCliente());
		this.pagado = venta.isPagado();
		this.fecha = Util.dateToString(venta.getFecha());
		this.facturado = venta.isFacturado();
		this.abonado = venta.getAbonado();
		this.total = venta.getTotal();
	}
}
