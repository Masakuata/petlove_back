package xatal.petlove.structures;

public enum ImagenType {
	PRODUCTO("producto"),
	CLIENTE("cliente");

	private final String text;

	ImagenType(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
}
