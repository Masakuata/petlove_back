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
import xatal.petlove.structures.NewCliente;
import xatal.petlove.structures.PublicCliente;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClienteService {
	private final ClienteRepository clienteRepository;
	private final DireccionRepository direccionRepository;

	public ClienteService(ClienteRepository clientes, DireccionRepository direcciones) {
		this.clienteRepository = clientes;
		this.direccionRepository = direcciones;
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

	public Cliente updateCliente(PublicCliente updatedCliente) {
		Cliente cliente = new Cliente(updatedCliente);
		this.clienteRepository.getById((long) updatedCliente.id).ifPresent(storedCliente -> {
			cliente.setDirecciones(storedCliente.getDirecciones());
			this.clienteRepository.save(cliente);
		});
		return cliente;
	}

	public Cliente saveCliente(NewCliente cliente) {
		return this.saveCliente(new Cliente(cliente));
	}

	public PublicCliente toPublicCliente(Cliente cliente) {
		return new PublicCliente(cliente);
	}

	public List<PublicCliente> toPublicCliente(List<Cliente> clientes) {
		return clientes
			.stream()
			.map(PublicCliente::new)
			.toList();
	}

	public Optional<Direccion> addDireccion(long idCliente, String newDireccion) {
		Cliente cliente = this.getById(idCliente);
		boolean repetida = cliente.getDirecciones()
			.stream()
			.anyMatch(direccion -> direccion.getDireccion().equalsIgnoreCase(newDireccion));
		if (!repetida) {
			Direccion direccion = new Direccion();
			direccion.setDireccion(newDireccion);
			cliente.getDirecciones().add(direccion);
			this.direccionRepository.save(direccion);
			this.clienteRepository.save(cliente);
			return Optional.of(direccion);
		}
		return Optional.empty();
	}

	public boolean updateDireccion(long idCliente, long idDireccion, String newDireccion) {
		Optional<Direccion> optionalDireccion = this.direccionRepository.getById(idDireccion);
		if (optionalDireccion.isPresent()) {
			Direccion direccion = optionalDireccion.get();
			direccion.setDireccion(newDireccion);
			this.direccionRepository.save(direccion);
			return true;
		}
		return false;
	}

	public boolean isDireccionRegistered(long idDireccion) {
		return this.direccionRepository.getById(idDireccion).isPresent();
	}

	public boolean isDireccionReferenced(long idDireccion) {
		return this.direccionRepository.isReferenced(idDireccion) > 0;
	}

	public void deleteDireccion(long idCliente, long idDireccion) {
		this.clienteRepository.getById(idCliente).ifPresent(cliente ->
			cliente.getDirecciones()
				.stream()
				.filter(direccion -> direccion.getId() == idDireccion)
				.findFirst()
				.ifPresent(direccion -> {
					cliente.getDirecciones().remove(direccion);
					this.direccionRepository.delete(direccion);
					this.clienteRepository.save(cliente);
				}));
	}

	public void deactivateDireccion(long idDireccion) {
		this.direccionRepository.deactivateDireccion(idDireccion);
	}

	private void saveClienteDirecciones(Cliente cliente) {
		List<Direccion> direcciones = new LinkedList<>();
		this.direccionRepository.saveAll(cliente.getDirecciones()).forEach(direcciones::add);
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
