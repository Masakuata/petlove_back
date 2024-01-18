package xatal.petlove.structures;

public class DetailedPrecio extends PublicPrecio {
	public String tipoCliente = "";

	public DetailedPrecio(PublicPrecio publicPrecio) {
		this.id = publicPrecio.id;
		this.precio = publicPrecio.precio;
	}
}
