package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Cliente;
import xatal.petlove.entities.Direccion;
import xatal.petlove.repositories.ClienteRepository;
import xatal.petlove.repositories.DireccionRepository;
import xatal.petlove.structures.ClienteMinimal;
import xatal.petlove.structures.PublicCliente;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
	private final ClienteRepository clienteRepository;
	private final DireccionRepository direcciones;

	private List<Cliente> clientesCache = null;

	public ClienteService(ClienteRepository clientes, DireccionRepository direcciones) {
		this.clienteRepository = clientes;
		this.direcciones = direcciones;
		this.ensureCache();
	}

	private void loadClientesCache() {
		this.clientesCache = this.clienteRepository.getAll();
	}

	private void ensureCache() {
		if (this.clientesCache == null) {
			this.loadClientesCache();
		}
	}

	public List<Cliente> getAll() {
		return this.clienteRepository.getAll();
	}

	public List<PublicCliente> getAllPublic() {
		return this.getAll()
			.stream()
			.map(PublicCliente::new)
			.collect(Collectors.toList());
	}

	public List<ClienteMinimal> getMinimal() {
		return this.clienteRepository.getMinimal()
			.stream().toList();
	}

	public List<Cliente> searchByName(String nombre, int size) {
//		Specification<Cliente> spec = this.addNombreSpecification(nombre, Specification.where(null));
		return this.clientesCache
			.stream()
			.filter(cliente -> cliente.getNombre().equalsIgnoreCase(nombre))
			.limit(size)
			.toList();
//		return this.clienteRepository.findAll(spec)
//			.stream()
//			.limit(size)
//			.toList();
	}

	public List<PublicCliente> searchByNamePublic(String nombre, int size) {
		return this.searchByName(nombre, size)
			.stream()
			.map(PublicCliente::new)
			.collect(Collectors.toList());
	}

	public Cliente saveCliente(Cliente cliente) {
		this.saveClienteDirecciones(cliente);
		this.loadClientesCache();
		return this.clienteRepository.save(cliente);
	}

	public Cliente saveCliente(PublicCliente cliente) {
		Cliente aux = this.saveCliente(new Cliente(cliente));
		this.loadClientesCache();
		return aux;
	}

	private void saveClienteDirecciones(Cliente cliente) {
		List<Direccion> direcciones = new LinkedList<>();
		this.direcciones.saveAll(cliente.getDirecciones()).forEach(direcciones::add);
		cliente.setDirecciones(direcciones);
		this.loadClientesCache();
	}

	public Cliente getById(Long id) {
		return this.clienteRepository.getById(id).orElse(null);
	}

	@Transactional
	public void removeById(int id) {
		this.clienteRepository.deleteById((long) id);
		this.loadClientesCache();
	}

	public boolean isIdRegistered(int id) {
		return this.clienteRepository.countById((long) id) > 0;
	}

	public boolean isEmailUsed(String email) {
		return email != null && !email.isEmpty() && this.clienteRepository.countByEmail(email) > 0;
	}

	private Specification<Cliente> addNombreSpecification(String nombre, Specification<Cliente> spec) {
		if (nombre != null && !nombre.isEmpty()) {
			spec = spec.and(((root, query, builder) ->
				builder.like(builder.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%")));
		}
		return spec;
	}
}
