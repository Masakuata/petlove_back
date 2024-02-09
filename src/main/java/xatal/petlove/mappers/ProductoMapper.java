package xatal.petlove.mappers;

import xatal.petlove.entities.Producto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ProductoMapper {
	public static Map<Long, Producto> mapIdProducto(List<Producto> productos) {
		return productos
			.stream()
			.collect(Collectors.toMap(Producto::getId, producto -> producto));
	}

}
