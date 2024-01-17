package xatal.petlove.reports;

import ch.qos.logback.core.joran.action.NOPAction;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.antlr.v4.runtime.misc.Pair;
import xatal.petlove.entities.Producto;
import xatal.petlove.entities.ProductoVenta;
import xatal.petlove.entities.Venta;
import xatal.petlove.services.ProductoService;
import xatal.petlove.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

public class PDFVentaReports {
	private static final String[] VENTA_HEADERS = {
		"ID VENTA", "CLIENTE", "PAGADO", "TOTAL", "ABONADO", "FECHA", "FACTURADO"
	};

	private static final String[] PRODUCTO_HEADERS = {
		"NOMBRE", "PRESENTACION", "TIPO MASCOTA", "RAZA", "PRECIO", "CANTIDAD"
	};

	private static final String PATH = "file.pdf";

	private final ProductoService productoService;

	public PDFVentaReports(ProductoService productoService) {
		this.productoService = productoService;
	}

	public void generateReportsFrom(List<Venta> ventas) {
		try (PdfWriter writer = new PdfWriter(PDFVentaReports.PATH)) {
			Document document = this.setupDocument(writer);

			document.add(this.buildVentasTable(ventas));
			document.add(this.getTitle("PRODUCTOS"));
			this.getProductosTables(ventas).forEach(block -> {
				document.add(block.a);
				document.add(block.b);
			});

			document.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Document setupDocument(PdfWriter writer) {
		Document document = new Document(new PdfDocument(writer));
		document.setFontSize(10F);
		document.add(this.getTitle("VENTAS"));
		return document;
	}

	private Paragraph getTitle(String title) {
		return new Paragraph(title)
			.setFontSize(20F);
	}

	private Paragraph getTitle(String left, String right) {
		Paragraph leftParagraph = new Paragraph(left)
			.setFontSize(20F)
			.setTextAlignment(TextAlignment.LEFT);
		Paragraph rightParagraph = new Paragraph(right)
			.setFontSize(20F)
			.setTextAlignment(TextAlignment.RIGHT);
		return leftParagraph.add(rightParagraph);
	}

	private Table buildVentasTable(List<Venta> ventas) {
		Table table = new Table(PDFVentaReports.VENTA_HEADERS.length);
		table.setWidth(UnitValue.createPercentValue(100));
		this.addHeaderCells(table, PDFVentaReports.VENTA_HEADERS);
		this.addVentaValues(ventas, table);
		return table;
	}

	private List<Pair<Paragraph, Table>> getProductosTables(List<Venta> ventas) {
		List<Pair<Paragraph, Table>> blocks = new LinkedList<>();

		ventas.forEach(venta ->
			blocks.add(new Pair<>(
				this.getTitle(venta.getCliente().getNombre(), Util.dateToString(venta.getFecha())),
				buildProductosTable(getVentaProductos(venta))
			)));
		return blocks;
	}

	private Table buildProductosTable(List<Producto> productos) {
		Table table = new Table(PDFVentaReports.PRODUCTO_HEADERS.length);
		table.setWidth(UnitValue.createPercentValue(100F));
		this.addHeaderCells(table, PDFVentaReports.PRODUCTO_HEADERS);
		this.addProductosValues(productos, table);
		return table;
	}

	private void addHeaderCells(Table table, String[] headers) {
		stream(headers).forEach(ventaHeader -> {
			Cell cell = new Cell();
			Paragraph text = new Paragraph(ventaHeader);
			text.setTextAlignment(TextAlignment.CENTER);
			cell.add(text);
			table.addHeaderCell(cell);
		});
	}

	private void addVentaValues(List<Venta> ventas, Table table) {
		ventas.forEach(venta -> {
			Cell ventaId = new Cell();
			ventaId.add(new Paragraph(String.valueOf(venta.getId())));

			Cell cliente = new Cell();
			cliente.add(new Paragraph(venta.getCliente().getNombre()));

			Cell pagado = new Cell();
			pagado.add(new Paragraph(venta.isPagado() ? "SI" : "NO"));

			Cell total = new Cell();
			total.add(new Paragraph("$" + venta.getTotal()));

			Cell abonado = new Cell();
			abonado.add(new Paragraph("$" + venta.getAbonado()));

			Cell fecha = new Cell();
			fecha.add(new Paragraph(Util.dateToString(venta.getFecha())));

			Cell facturado = new Cell();
			facturado.add(new Paragraph(venta.isFacturado() ? "SI" : "NO"));

			table.addCell(ventaId);
			table.addCell(cliente);
			table.addCell(pagado);
			table.addCell(total);
			table.addCell(abonado);
			table.addCell(fecha);
			table.addCell(facturado);
		});
	}

	private void addProductosValues(List<Producto> productos, Table table) {
		productos.forEach(producto -> {
			Cell nombre = new Cell();
			nombre.add(new Paragraph(producto.getNombre()));

			Cell presentacion = new Cell();
			presentacion.add(new Paragraph(producto.getPresentacion()));

			Cell mascota = new Cell();
			mascota.add(new Paragraph(producto.getTipoMascota()));

			Cell raza = new Cell();
			raza.add(new Paragraph(producto.getRaza()));

			Cell precio = new Cell();
			precio.add(new Paragraph("$" + producto.getPrecio()));

			Cell cantidad = new Cell();
			cantidad.add(new Paragraph(String.valueOf(producto.getCantidad())));

			table.addCell(nombre);
			table.addCell(presentacion);
			table.addCell(mascota);
			table.addCell(raza);
			table.addCell(precio);
			table.addCell(cantidad);
		});
	}

	private List<Producto> getVentaProductos(Venta venta) {
		List<Producto> list = new ArrayList<>();
		venta.getProductos().forEach(productoVenta -> {
			Optional<Producto> byIdAndTipoCliente = this.productoService.getByIdAndTipoCliente(
				Math.toIntExact(productoVenta.getProducto()),
				venta.getCliente().getTipoCliente());
			if (byIdAndTipoCliente.isPresent()) {
				Producto producto = byIdAndTipoCliente.get();
				producto.setCantidad(productoVenta.getCantidad());
				list.add(producto);
			}
		});
		return list;
	}
}
