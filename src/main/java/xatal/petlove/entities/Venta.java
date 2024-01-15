package xatal.petlove.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import xatal.petlove.reports.ReportableField;
import xatal.petlove.reports.ReportableList;

import java.util.Date;
import java.util.List;

@Entity
public class Venta {
	@ReportableField(headerName = "ID VENTA")
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ReportableField(headerName = "CLIENTE", getValueFrom = "getNombre")
	@ManyToOne
	@JoinColumn(name = "cliente", nullable = false)
	private Cliente cliente;

	@Column(name = "vendedor", nullable = false)
	private Long vendedor;

	@ReportableField(headerName = "PAGADO")
	@Column(name = "pagado", nullable = false)
	private boolean pagado = false;

	@ReportableField(headerName = "FECHA", isDate = true)
	@Column(name = "fecha", nullable = false)
	private Date fecha = new Date();

	@ReportableField(headerName = "FACTURADO")
	@Column(name = "facturado", nullable = false)
	private boolean facturado = false;

	@ReportableField(headerName = "ABONADO")
	@Column(name = "abonado", nullable = false)
	private float abonado = 0F;

	@ReportableField(headerName = "TOTAL")
	@Column(name = "total", nullable = false)
	private float total = 0F;

//	@ReportableList(headerName = "PRODUCTOS")
	@OneToMany
	private List<ProductoVenta> productos;

	public Venta() {
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Long getVendedor() {
		return vendedor;
	}

	public void setVendedor(Long vendedor) {
		this.vendedor = vendedor;
	}

	public boolean isPagado() {
		return pagado;
	}

	public void setPagado(boolean pagado) {
		this.pagado = pagado;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public boolean isFacturado() {
		return facturado;
	}

	public void setFacturado(boolean facturado) {
		this.facturado = facturado;
	}

	public float getAbonado() {
		return abonado;
	}

	public void setAbonado(float abonado) {
		this.abonado = abonado;
	}

	public float getTotal() {
		return total;
	}

	public void setTotal(float total) {
		this.total = total;
	}

	public List<ProductoVenta> getProductos() {
		return productos;
	}

	public void setProductos(List<ProductoVenta> productos) {
		this.productos = productos;
	}
}
