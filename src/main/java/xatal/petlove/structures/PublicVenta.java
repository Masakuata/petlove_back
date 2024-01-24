package xatal.petlove.structures;

import java.util.ArrayList;
import java.util.List;

public class PublicVenta {
	public Long id = -1L;
	public Long cliente = -1L;
	public Long vendedor = -1L;
	public boolean pagado = false;
	public String fecha = "01-01-1970";
	public boolean facturado = false;
	public float abonado = 0F;
	public float total = 0F;
	public String direccion;
	public List<PublicProductoVenta> productos = new ArrayList<>();

	public PublicVenta() {
	}
}
