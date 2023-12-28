package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Cliente;
import xatal.sharedz.repositories.ClienteRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private List<Cliente> clientesCache = null;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public List<Cliente> getAll() {
        if (this.clientesCache == null) {
            this.clientesCache = this.clientes.getAll();
        }
        return this.clientesCache;
    }

    public List<Cliente> searchByName(String nombre) {
        if (this.clientesCache == null) {
            this.clientesCache = this.clientes.getAll();
        }
        return this.clientesCache
                .stream()
                .filter(cliente ->
                        cliente.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }

    public Cliente saveCliente(Cliente cliente) {
        this.clientesCache = null;
        return this.clientes.save(cliente);
    }

    @Transactional
    public void removeById(int id) {
        this.clientesCache = null;
        this.clientes.deleteById((long) id);
    }

    public boolean isIdRegistered(int id) {
        return this.clientes.countById((long) id) > 0;
    }

    public boolean isEmailUsed(String email) {
        return this.clientes.countByEmail(email) > 0;
    }
}
