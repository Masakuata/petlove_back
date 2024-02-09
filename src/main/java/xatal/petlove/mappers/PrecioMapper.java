package xatal.petlove.mappers;

import org.springframework.stereotype.Component;
import xatal.petlove.entities.Precio;
import xatal.petlove.entities.TipoCliente;
import xatal.petlove.services.TipoClienteService;
import xatal.petlove.structures.DetailedPrecio;
import xatal.petlove.structures.PublicPrecio;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PrecioMapper {
	private final TipoClienteService tipoClienteService;

	public PrecioMapper(TipoClienteService tipoClienteService) {
		this.tipoClienteService = tipoClienteService;
	}

	public List<DetailedPrecio> publicToDetailed(List<PublicPrecio> publicPrecios) {
		Map<Long, TipoCliente> tipoClienteMap = TipoClienteMapper.mapTipoCliente(this.tipoClienteService.getTiposCliente());
		return publicPrecios
			.stream()
			.map(publicPrecio -> {
				DetailedPrecio aux = new DetailedPrecio(publicPrecio);
				if (tipoClienteMap.containsKey((long) aux.id)) {
					aux.tipoCliente = tipoClienteMap.get((long) aux.id).getTipoCliente();
				}
				return aux;
			})
			.toList();
	}

	public List<DetailedPrecio> precioToDetailed(List<Precio> precios) {
		Map<Long, TipoCliente> tipoClienteMap = TipoClienteMapper.mapTipoCliente(this.tipoClienteService.getTiposCliente());
		return precios
			.stream()
			.map(precio -> {
				DetailedPrecio aux = new DetailedPrecio(precio);
				if (tipoClienteMap.containsKey((long) aux.id)) {
					aux.tipoCliente = tipoClienteMap.get((long) aux.id).getTipoCliente();
				}
				return aux;
			})
			.toList();
	}

	public Map<Long, Precio> mapProductoPrecio(List<Precio> precios) {
		return precios
			.stream()
			.collect(Collectors.toMap(Precio::getProducto, precio -> precio));
	}
}
