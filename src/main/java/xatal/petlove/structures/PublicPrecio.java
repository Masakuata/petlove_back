package xatal.petlove.structures;

import xatal.petlove.entities.Precio;

public class PublicPrecio {
    public int id;
    public float precio;

    public PublicPrecio() {

    }

    public PublicPrecio(Precio precio) {
        this.id = Math.toIntExact(precio.getCliente());
        this.precio = precio.getPrecio();
    }
}
