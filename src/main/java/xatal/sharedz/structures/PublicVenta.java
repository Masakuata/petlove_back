package xatal.sharedz.structures;

import xatal.sharedz.entities.Venta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PublicVenta {
	public Long id = -1L;
	public Long cliente = -1L;
	public boolean pagado = false;
	public String fecha = "01-01-1970";
	public boolean facturado = false;
	public float abonado = 0F;
	public float total = 0F;
	public List<PublicProductoVenta> productos = new ArrayList<>();

	public PublicVenta() {
	}

	public PublicVenta(Venta venta) {
		this.id = venta.getId();
		this.cliente = venta.getCliente().getId();
		this.pagado = venta.isPagado();
		this.fecha = new SimpleDateFormat("dd/MM/yyyy").format(venta.getFecha());
		this.facturado = venta.isFacturado();
		this.abonado = venta.getAbonado();
		this.total = venta.getTotal();
		venta.getProductos().forEach(producto -> this.productos.add(new PublicProductoVenta(producto)));
	}
}
