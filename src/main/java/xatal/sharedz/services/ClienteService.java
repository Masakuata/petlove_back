package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Cliente;
import xatal.sharedz.repositories.ClienteRepository;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clientes;

    public ClienteService(ClienteRepository clientes) {
        this.clientes = clientes;
    }

    public List<Cliente> getAll() {
        return this.clientes.getAll();
    }

    public Cliente saveCliente(Cliente cliente) {
        return this.clientes.save(cliente);
    }

    @Transactional
    public void removeById(int id) {
        this.clientes.deleteById((long) id);
    }

    public boolean isIdRegistered(int id) {
        return this.clientes.countById((long) id) > 0;
    }

    public boolean isEmailUsed(String email) {
        return this.clientes.countByEmail(email) > 0;
    }
}
