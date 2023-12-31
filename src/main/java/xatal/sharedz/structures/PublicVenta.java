package xatal.sharedz.structures;

import xatal.sharedz.entities.Venta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PublicVenta {
    public int id = -1;
    public int cliente = -1;
    public boolean pagado = false;
    public String fecha = "01-01-1970";
    public boolean facturado = false;
    public List<PublicProductoVenta> productos = new ArrayList<>();

    public float total = 0;

    public PublicVenta() {
    }

    public PublicVenta(Venta venta) {
        this.id = Math.toIntExact(venta.getId());
        this.cliente = Math.toIntExact(venta.getCliente().getId());
        this.pagado = venta.isPagado();
        this.fecha = new SimpleDateFormat("dd/MM/yyyy").format(venta.getFecha());
        this.facturado = venta.isFacturado();
        venta.getProductos().forEach(producto -> this.productos.add(new PublicProductoVenta(producto)));
    }
}
