package xatal.petlove.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import xatal.petlove.structures.NewCliente;
import xatal.petlove.structures.PublicCliente;

import java.util.List;
import java.util.Optional;

@Entity
public class Cliente {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name = "tipoCliente", nullable = false)
	private Integer tipoCliente;

	@Column(name = "nombre", nullable = false)
	private String nombre;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "RFC")
	private String RFC;

	@OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private List<Direccion> direcciones;

	@Column(name = "telefono", nullable = false)
	private String telefono;

	public Cliente() {
	}

	public Cliente(PublicCliente cliente) {
		this.id = (long) cliente.id;
		this.tipoCliente = cliente.tipoCliente;
		this.nombre = cliente.nombre;
		this.email = cliente.email;
		this.RFC = cliente.RFC;
		this.telefono = cliente.telefono;
		this.direcciones = cliente.direcciones;
	}

	public Cliente(NewCliente cliente) {
		this.id = (long) cliente.id;
		this.tipoCliente = cliente.tipoCliente;
		this.nombre = cliente.nombre;
		this.email = cliente.email;
		this.RFC = cliente.RFC;
		this.telefono = cliente.telefono;
		this.direcciones = cliente.direcciones
			.stream()
			.map(direccion -> {
				Direccion aux = new Direccion();
				aux.setDireccion(direccion);
				return aux;
			})
			.toList();
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public int getTipoCliente() {
		return tipoCliente;
	}

	public void setTipoCliente(int tipoCliente) {
		this.tipoCliente = tipoCliente;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRFC() {
		return RFC;
	}

	public void setRFC(String RFC) {
		this.RFC = RFC;
	}

	public List<Direccion> getDirecciones() {
		return direcciones;
	}

	public void setDirecciones(List<Direccion> direcciones) {
		this.direcciones = direcciones;
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public Optional<Direccion> getDireccionByString(String direccion) {
		return this.direcciones
			.stream()
			.filter(direccion1 -> direccion1.getDireccion().equalsIgnoreCase(direccion))
			.findFirst();
	}

	public Optional<Direccion> getDireccionById(long id) {
		return this.direcciones
			.stream()
			.filter(direccion -> direccion.getId() == id)
			.findFirst();
	}
}
