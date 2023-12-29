package xatal.sharedz.services;

import org.springframework.stereotype.Service;
import xatal.sharedz.entities.ProductoVenta;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.repositories.ProductoVentaRepository;
import xatal.sharedz.repositories.VentaRepository;
import xatal.sharedz.structures.PublicVenta;
import xatal.sharedz.util.Util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ProductoVentaRepository productoVentaRepository;
    private final ClienteService clienteService;

    public VentaService(
            VentaRepository ventaRepository,
            ProductoVentaRepository productoVentaRepository,
            ClienteService clienteService) {
        this.ventaRepository = ventaRepository;
        this.productoVentaRepository = productoVentaRepository;
        this.clienteService = clienteService;
    }

    public List<Venta> getAll() {
        return this.ventaRepository.getAll();
    }

    public List<Venta> searchVentas(String nombreCliente, Date fechaVenta) {
        Stream<Venta> ventas = this.ventaRepository.getAll().stream();
        Predicate<Venta> filtros = venta -> true;
        if (nombreCliente != null && !nombreCliente.isEmpty()) {
            filtros.and(venta ->
                    Util.containsAnyCase(venta.getCliente().getNombre(), nombreCliente));
        }
        if (fechaVenta != null) {
            filtros.and(venta -> Util.compareDates(venta.getFecha(), fechaVenta));
        }
        return ventas.filter(filtros).collect(Collectors.toList());
    }

    public Venta newVenta(Venta venta) {
        List<ProductoVenta> productosGuardados = new LinkedList<>();
        this.productoVentaRepository.saveAll(venta.getProductos())
                .forEach(productosGuardados::add);
        venta.setProductos(productosGuardados);
        return this.ventaRepository.save(venta);
    }

    public Venta newVenta(PublicVenta publicVenta) {
        return this.newVenta(this.buildFromPublicVenta(publicVenta));
    }

    public Venta buildFromPublicVenta(PublicVenta publicVenta) {
        Venta aux = new Venta();
        aux.setCliente(this.clienteService.getById(publicVenta.cliente));
        aux.setPagado(publicVenta.pagado);
        try {
            aux.setFecha(new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).parse(publicVenta.fecha));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        aux.setProductos(publicVenta.productos);
        return aux;
    }
}
