package xatal.sharedz.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.structures.Attachment;
import xatal.sharedz.structures.MIMEType;
import xatal.sharedz.util.Util;
import xatal.sharedz.util.XEmail;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class ReportService {
    private final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private static final String SENDER_EMAIL = "edsonmanuelcarballovera@gmail.com";
    private static final String VENTA_HEADER = "ID VENTA,CLIENTE,PAGADO,FECHA,FACTURADO\n";
    private static final String PRODUCTO_HEADER = "NOMBRE,PRESENTACION,TIPO MASCOTA,RAZA,PRECIO\n";
    private final VentaService ventaService;
    private final ProductoService productoService;

    public ReportService(VentaService ventaService, ProductoService productoService) {
        this.ventaService = ventaService;
        this.productoService = productoService;
    }

    public void ventasFromDate(Date date, String recipientName, String recipientEmail) {
        final String stringDate = Util.dateToString(date);
        final List<Venta> ventas = this.ventaService.searchVentas(null, date);
        final List<Attachment> attachments = new LinkedList<>();
        attachments.add(new Attachment(
                "ventas." + stringDate + ".csv",
                getVentasBytes(stringDate, ventas),
                MIMEType.TEXT_CSV
        ));

        ventas.forEach(venta ->
                attachments.add(new Attachment(
                        "productos.venta." + venta.getCliente().getNombre() + "." + stringDate + ".csv",
                        this.getProductosBytes(venta),
                        MIMEType.TEXT_CSV
                ))
        );

        sendEmailWithAttachment(
                stringDate,
                recipientName,
                recipientEmail,
                attachments
        );
    }

    private byte[] getVentasBytes(String stringDate, List<Venta> ventas) {
        StringBuilder file = new StringBuilder(ReportService.VENTA_HEADER);
        ventas.forEach(venta -> this.appendVentaDetails(file, venta, stringDate));
        return file.toString().getBytes();
    }

    private byte[] getProductosBytes(Venta venta) {
        StringBuilder file = new StringBuilder(ReportService.PRODUCTO_HEADER);
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
        file.append(producto.getPrecio()).append(",");
        file.append("\n");
    }

    private void appendVentaDetails(StringBuilder file, Venta venta, String stringDate) {
        file.append(venta.getId()).append(",");
        file.append(venta.getCliente().getNombre()).append(",");
        file.append(venta.isPagado() ? "SI" : "NO").append(",");
        file.append(stringDate).append(",");
        file.append(venta.isFacturado() ? "SI" : "NO");
        file.append("\n");
    }

    private void sendEmailWithAttachment(
            final String stringDate,
            final String recipientName,
            final String recipientEmail,
            final Attachment attachment
    ) {
        List<Attachment> attachments = new LinkedList<>();
        attachments.add(attachment);

        this.sendEmailWithAttachment(
                stringDate,
                recipientName,
                recipientEmail,
                attachments
        );
    }

    private void sendEmailWithAttachment(
            final String stringDate,
            final String recipientName,
            final String recipientEmail,
            final List<Attachment> attachments
    ) {
        XEmail email = new XEmail();
        email.setFrom(ReportService.SENDER_EMAIL);
        email.setSubject("Ventas del dia " + stringDate);
        email.addRecipient(recipientName, recipientEmail);
        email.setMessage("A continuacion encontrara adjunto las ventas del dia " + stringDate);
        attachments.forEach(attachment ->
                email.addAttachment(attachment.getFilename(), attachment.getBytes(), attachment.getMimeType()));
        email.send();
    }

}
