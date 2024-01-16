package xatal.petlove.structures;

import xatal.petlove.entities.Cliente;
import xatal.petlove.entities.Direccion;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PublicCliente {
    public int id = -1;
    public String nombre = "";
    public String email = "";
    public String RFC = "";
    public List<String> direcciones = new LinkedList<>();
    public String telefono = "";
    public int tipoCliente = 1;

    public PublicCliente() {
    }

    public PublicCliente(Cliente cliente) {
        this.id = Math.toIntExact(cliente.getId());
        this.nombre = cliente.getNombre();
        this.email = cliente.getEmail();
        this.RFC = cliente.getRFC();
        this.direcciones = cliente.getDirecciones()
                .stream()
                .map(Direccion::getDireccion)
                .collect(Collectors.toList());
        this.telefono = cliente.getTelefono();
        this.tipoCliente = cliente.getTipoCliente();
    }
}
