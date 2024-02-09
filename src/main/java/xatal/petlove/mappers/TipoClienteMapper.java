package xatal.petlove.mappers;

import xatal.petlove.entities.Precio;
import xatal.petlove.entities.TipoCliente;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TipoClienteMapper {
	public static Map<Long, TipoCliente> mapTipoCliente(List<TipoCliente> tiposCliente) {
		return tiposCliente
			.stream()
			.collect(
				Collectors.toMap(TipoCliente::getId, tipoCliente -> tipoCliente));
	}

	public static Map<Long, Precio> mapTipoClientePrecio(List<Precio> precios) {
		return precios
			.stream()
			.collect(
				Collectors.toMap(Precio::getCliente, precio -> precio));
	}
}
