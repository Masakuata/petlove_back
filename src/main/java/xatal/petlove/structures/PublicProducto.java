package xatal.petlove.structures;

import xatal.petlove.entities.Producto;

public class PublicProducto {
    public String nombre = "";
    public String presentacion = "";
    public String tipoMascota = "";
    public String raza = "";
    public float precio = -1;
    public int cantidad = 0;

    public PublicProducto() {
    }

    public PublicProducto(Producto producto) {
        this.nombre = producto.getNombre();
        this.presentacion = producto.getPresentacion();
        this.tipoMascota = producto.getTipoMascota();
        this.raza = producto.getRaza();
        this.precio = producto.getPrecio();
        this.cantidad = producto.getCantidad();
    }
}
