package xatal.petlove.services;

import jakarta.transaction.Transactional;
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
		this.ensureClientesCacheLoaded();
	}

	private void ensureClientesCacheLoaded() {
		if (this.clientesCache == null) {
			this.clientesCache = this.clienteRepository.getAll();
		}
	}

	private void refreshClientesCacheAsync() {
		new Thread(() -> this.clientesCache = this.clienteRepository.getAll()).start();
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
		this.ensureClientesCacheLoaded();
		String lowercaseNombre = nombre.toLowerCase();
		return this.clientesCache
			.stream()
			.filter(cliente -> cliente.getNombre().toLowerCase().contains(lowercaseNombre))
			.limit(size)
			.collect(Collectors.toList());
	}

	public List<PublicCliente> searchByNamePublic(String nombre, int size) {
		return this.searchByName(nombre, size)
			.stream()
			.map(PublicCliente::new)
			.collect(Collectors.toList());
	}

	public Cliente saveCliente(Cliente cliente) {
		this.saveClienteDirecciones(cliente);
		Cliente savedCliente = this.clienteRepository.save(cliente);
		this.refreshClientesCacheAsync();
		return savedCliente;
	}

	public Cliente saveCliente(PublicCliente cliente) {
		return this.saveCliente(new Cliente(cliente));
	}

	private void saveClienteDirecciones(Cliente cliente) {
		List<Direccion> direcciones = new LinkedList<>();
		this.direcciones.saveAll(cliente.getDirecciones()).forEach(direcciones::add);
		cliente.setDirecciones(direcciones);
	}

	public Cliente getById(Long id) {
		return this.clienteRepository.getById(id).orElse(null);
	}

	@Transactional
	public void removeById(int id) {
		this.clienteRepository.deleteById((long) id);
		this.refreshClientesCacheAsync();
	}

	public boolean isIdRegistered(int id) {
		return this.clienteRepository.countById((long) id) > 0;
	}

	public boolean isEmailUsed(String email) {
		if (email == null || email.isEmpty()) {
			return false;
		}
		return this.clienteRepository.countByEmail(email) > 0;
	}
}
