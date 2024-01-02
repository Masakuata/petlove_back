package xatal.sharedz.reports;

import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.services.ProductoService;
import xatal.sharedz.structures.Attachment;
import xatal.sharedz.structures.MIMEType;
import xatal.sharedz.util.Util;

import java.util.LinkedList;
import java.util.List;

@Service
public class VentasReports extends XReport {
    private static final String VENTA_HEADER = "ID VENTA,CLIENTE,PAGADO,FECHA,FACTURADO\n";
    private static final String PRODUCTO_HEADER = "NOMBRE,PRESENTACION,TIPO MASCOTA,RAZA,PRECIO\n";
    private final ProductoService productoService;

    public VentasReports(ProductoService productoService) {
        this.productoService = productoService;
    }

    public void reporteFrom(List<Venta> ventas, String recipientName, String recipientEmail) {
        final List<Attachment> attachments = new LinkedList<>();
        attachments.add(new Attachment("ventas.csv", getVentasBytes(ventas), MIMEType.TEXT_CSV));

        ventas.forEach(venta -> attachments.add(new Attachment(
                "productos.venta" + venta.getCliente().getNombre() + "." + Util.dateToString(venta.getFecha()) + ".csv",
                this.getProductosBytes(venta),
                MIMEType.TEXT_CSV
        )));

        this.sendEmailWithAttachment(
                "Reporte de Ventas",
                "Adjuntos se encuentran los reportes solicitados",
                recipientName,
                recipientEmail,
                attachments
        );
    }

    private byte[] getVentasBytes(List<Venta> ventas) {
        StringBuilder file = new StringBuilder(VentasReports.VENTA_HEADER);
        ventas.forEach(venta -> this.appendVentaDetails(file, venta));
        return file.toString().getBytes();
    }

    private byte[] getProductosBytes(Venta venta) {
        StringBuilder file = new StringBuilder(VentasReports.PRODUCTO_HEADER);
        List<Producto> productos = new LinkedList<>();
        venta.getProductos().forEach(productoVenta ->
                this.productoService
                        .getByIdAndTipoCliente(Math.toIntExact(productoVenta.getId()), venta.getCliente().getTipoCliente())
                        .ifPresent(productos::add));
        productos.forEach(producto -> this.appendProductoDetails(file, producto));
        return file.toString().getBytes();
    }

    private void appendProductoDetails(StringBuilder file, Producto producto) {
        file.append(producto.getNombre()).append(",");
        file.append(producto.getPresentacion()).append(",");
        file.append(producto.getTipoMascota()).append(",");
        file.append(producto.getRaza()).append(",");
        file.append(producto.getPrecio());
        file.append("\n");
    }

    private void appendVentaDetails(StringBuilder file, Venta venta) {
        file.append(venta.getId()).append(",");
        file.append(venta.getCliente().getNombre()).append(",");
        file.append(venta.isPagado() ? "SI" : "NO").append(",");
        file.append(Util.dateToString(venta.getFecha())).append(",");
        file.append(venta.isFacturado() ? "SI" : "NO");
        file.append("\n");
    }
}
