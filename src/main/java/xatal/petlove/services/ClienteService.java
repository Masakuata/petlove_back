package xatal.petlove.services;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.petlove.entities.Cliente;
import xatal.petlove.entities.Direccion;
import xatal.petlove.repositories.ClienteRepository;
import xatal.petlove.repositories.DireccionRepository;
import xatal.petlove.structures.PublicCliente;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
	private final ClienteRepository clienteRepository;
	private final DireccionRepository direcciones;

	public ClienteService(ClienteRepository clientes, DireccionRepository direcciones) {
		this.clienteRepository = clientes;
		this.direcciones = direcciones;
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

	public List<Cliente> search(
		Integer id,
		String nombre,
		int size,
		int pag
	) {
		Specification<Cliente> spec = Specification.where(null);
		spec = this.addIdClienteSpec(id, spec);
		spec = this.addNombreSpecification(nombre, spec);

		Pageable pageable = PageRequest.of(pag, size);
		return this.clienteRepository.findAll(spec, pageable).stream().toList();
	}

	public Cliente saveCliente(Cliente cliente) {
		this.saveClienteDirecciones(cliente);
		return this.clienteRepository.save(cliente);
	}

	public Cliente saveCliente(PublicCliente cliente) {
		return this.saveCliente(new Cliente(cliente));
	}

	public List<PublicCliente> toPublicCliente(List<Cliente> clientes) {
		return clientes
			.stream()
			.map(PublicCliente::new)
			.toList();
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
	}

	public boolean isIdRegistered(int id) {
		return this.clienteRepository.countById((long) id) > 0;
	}

	public boolean isEmailUsed(String email) {
		return email != null && !email.isEmpty() && this.clienteRepository.countByEmail(email) > 0;
	}

	private Specification<Cliente> addIdClienteSpec(Integer id, Specification<Cliente> spec) {
		if (id != null) {
			spec = spec.and(((root, query, builder) -> builder.equal(root.get("id"), id)));
		}
		return spec;
	}

	private Specification<Cliente> addNombreSpecification(String nombre, Specification<Cliente> spec) {
		if (nombre != null && !nombre.isEmpty()) {
			spec = spec.and(((root, query, builder) ->
				builder.like(builder.lower(root.get("nombre")), "%" + nombre.toLowerCase() + "%")));
		}
		return spec;
	}
}
