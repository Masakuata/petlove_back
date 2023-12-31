package xatal.sharedz.structures;

import xatal.sharedz.entities.Precio;

public class PublicPrecio {
    public int tipoCliente;
    public float precio;

    public PublicPrecio() {

    }

    public PublicPrecio(Precio precio) {
        this.tipoCliente = Math.toIntExact(precio.getCliente());
        this.precio = precio.getPrecio();
    }
}
