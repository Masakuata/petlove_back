package xatal.sharedz.structures;

import xatal.sharedz.entities.Producto;

public class NewProducto {
    public String nombre;
    public String presentacion;
    public String tipoMascota;
    public String raza;
    public float precio;

    public int cantidad;

    public NewProducto() {
    }

    public NewProducto(Producto producto) {
        this.nombre = producto.getNombre();
        this.presentacion = producto.getPresentacion();
        this.tipoMascota = producto.getTipoMascota();
        this.raza = producto.getRaza();
        this.precio = producto.getPrecio();
        this.cantidad = producto.getCantidad();
    }

    public boolean isValid() {
        return this.nombre != null && !this.nombre.isEmpty()
                && this.presentacion != null && !this.presentacion.isEmpty()
                && this.tipoMascota != null && !this.tipoMascota.isEmpty();
    }
}
