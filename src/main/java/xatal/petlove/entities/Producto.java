package xatal.petlove.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import xatal.petlove.structures.PublicProducto;

@Entity
public class Producto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "presentacion", nullable = false)
	private String presentacion;

	@Column(name = "tipo_mascota", nullable = false)
	private String tipoMascota;

	@Column(name = "raza", nullable = false)
	private String raza;

	@Column(name = "precio", nullable = false)
	private float precio;

	@Column(name = "cantidad", nullable = false)
	private int cantidad = 0;

	@Column(name = "peso", nullable = false)
	private float peso = 0F;

	@Column(name = "status", nullable = false)
	private boolean status = true;

	public Producto(PublicProducto newProducto) {
		this.nombre = newProducto.nombre;
		this.presentacion = newProducto.presentacion;
		this.tipoMascota = newProducto.tipoMascota;
		this.raza = newProducto.raza;
		this.precio = newProducto.precio;
		this.cantidad = newProducto.cantidad;
		this.peso = newProducto.peso;
	}

	public Producto() {
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPresentacion() {
		return presentacion;
	}

	public void setPresentacion(String presentacion) {
		this.presentacion = presentacion;
	}

	public String getTipoMascota() {
		return tipoMascota;
	}

	public void setTipoMascota(String tipoMascota) {
		this.tipoMascota = tipoMascota;
	}

	public String getRaza() {
		return raza;
	}

	public void setRaza(String raza) {
		this.raza = raza;
	}

	public float getPrecio() {
		return precio;
	}

	public void setPrecio(float precio) {
		this.precio = precio;
	}

	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}

	public float getPeso() {
		return peso;
	}

	public void setPeso(float peso) {
		this.peso = peso;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}
}
