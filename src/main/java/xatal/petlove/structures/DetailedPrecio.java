package xatal.petlove.structures;

import xatal.petlove.entities.Precio;

public class DetailedPrecio extends PublicPrecio {
	public String tipoCliente = "";

	public DetailedPrecio(PublicPrecio publicPrecio) {
		this.id = publicPrecio.id;
		this.precio = publicPrecio.precio;
	}

	public DetailedPrecio(Precio precio) {
		this.id = Math.toIntExact(precio.getId());
		this.precio = precio.getPrecio();
	}
}
