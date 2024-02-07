package xatal.petlove.services;

import org.springframework.stereotype.Service;
import xatal.petlove.entities.TipoCliente;
import xatal.petlove.repositories.TipoClienteRepository;

import java.util.List;

@Service
public class TipoClienteService {
	private final TipoClienteRepository tipoClienteRepository;

	public TipoClienteService(TipoClienteRepository tipoClienteRepository) {
		this.tipoClienteRepository = tipoClienteRepository;
	}

	public List<TipoCliente> getTiposCliente() {
		return this.tipoClienteRepository.getAll();
	}

	public TipoCliente newTipoCliente(String nombre) {
		TipoCliente tipoCliente = new TipoCliente();
		tipoCliente.setTipoCliente(nombre);
		return this.tipoClienteRepository.save(tipoCliente);
	}
}
