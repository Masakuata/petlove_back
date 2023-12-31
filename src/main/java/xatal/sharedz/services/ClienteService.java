package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Cliente;
import xatal.sharedz.entities.Direccion;
import xatal.sharedz.repositories.ClienteRepository;
import xatal.sharedz.repositories.DireccionRepository;
import xatal.sharedz.structures.ClienteMinimal;
import xatal.sharedz.structures.PublicCliente;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClienteService {
    private final ClienteRepository clientes;
    private final DireccionRepository direcciones;
    private List<Cliente> clientesCache = null;

    public ClienteService(ClienteRepository clientes, DireccionRepository direcciones) {
        this.clientes = clientes;
        this.direcciones = direcciones;
    }

    private void ensureClientesCacheLoaded() {
        if (this.clientesCache == null) {
            this.clientesCache = this.clientes.getAll();
        }
    }

    private void refreshClientesCacheAsync() {
        new Thread(() -> this.clientesCache = this.clientes.getAll()).start();
    }

    public List<Cliente> getAll() {
        this.ensureClientesCacheLoaded();
        return this.clientesCache;
    }

    public List<PublicCliente> getAllPublic() {
        return this.getAll()
                .stream()
                .map(PublicCliente::new)
                .collect(Collectors.toList());
    }

    public List<ClienteMinimal> getMinimal() {
        return this.clientes.getMinimal()
                .stream().toList();
    }

    public List<Cliente> searchByName(String nombre, int size) {
        this.ensureClientesCacheLoaded();
        String lowercaseNombre = nombre.toLowerCase();
        return this.clientesCache
                .stream()
                .filter(cliente ->
                        cliente.getNombre().toLowerCase().contains(lowercaseNombre))
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
        Cliente savedCliente = this.clientes.save(cliente);
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

    public Cliente getById(int id) {
        return this.clientes.getById((long) id).orElse(null);
    }

    @Transactional
    public void removeById(int id) {
        this.clientes.deleteById((long) id);
        this.refreshClientesCacheAsync();
    }

    public boolean isIdRegistered(int id) {
        return this.clientes.countById((long) id) > 0;
    }

    public boolean isEmailUsed(String email) {
        return this.clientes.countByEmail(email) > 0;
    }
}
