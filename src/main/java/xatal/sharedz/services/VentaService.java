package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Abono;
import xatal.sharedz.entities.ProductoVenta;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.repositories.AbonoRepository;
import xatal.sharedz.repositories.ProductoVentaRepository;
import xatal.sharedz.repositories.VentaRepository;
import xatal.sharedz.structures.PublicAbono;
import xatal.sharedz.structures.PublicVenta;
import xatal.sharedz.util.Util;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ProductoVentaRepository productoVentaRepository;
    private final AbonoRepository abonoRepository;
    private final ClienteService clienteService;

    public VentaService(
            VentaRepository ventaRepository,
            ProductoVentaRepository productoVentaRepository, AbonoRepository abonoRepository,
            ClienteService clienteService) {
        this.ventaRepository = ventaRepository;
        this.productoVentaRepository = productoVentaRepository;
        this.abonoRepository = abonoRepository;
        this.clienteService = clienteService;
    }

    public List<Venta> getAll() {
        return this.ventaRepository.getAll();
    }

    public List<Venta> searchVentas(
            String nombreCliente,
//            String nombreProducto,
            Integer year,
            Integer month,
            Integer day
    ) {
        Specification<Venta> spec = Specification.where(null);
        spec = this.addNombreClienteSpecification(nombreCliente, spec);
        spec = this.addYearSpecification(year, spec);
        spec = this.addMonthSpecification(month, spec);
        spec = this.addDaySpecification(day, spec);
        return this.ventaRepository.findAll(spec);
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

    public Abono saveAbono(Abono abono) {
        return this.abonoRepository.save(abono);
    }

    public Abono saveAbono(PublicAbono abono) {
        return this.saveAbono(new Abono(abono));
    }

    public List<Abono> getAbonos(int idVenta) {
        return this.abonoRepository.findByVenta((long) idVenta);
    }

    public boolean isAbonoRegistered(int idAbono) {
        return this.abonoRepository.countById((long) idAbono) > 0;
    }

    public Venta updateVenta(PublicVenta publicVenta) {
        Venta storedVenta = this.getById(publicVenta.id);
        if (storedVenta != null) {
            return this.updateVenta(publicVenta, storedVenta);
        }
        return null;
    }

    public Venta updateVenta(PublicVenta publicVenta, Venta storedVenta) {
        if (publicVenta.cliente != -1) {
            storedVenta.setCliente(this.clienteService.getById(publicVenta.cliente));
        }
        if (publicVenta.fecha != null
                && !publicVenta.fecha.isEmpty()
                && !publicVenta.fecha.equals("01-01-1970")) {
            try {
                storedVenta.setFecha(Util.dateFromString(publicVenta.fecha));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        }
        storedVenta.setPagado(publicVenta.pagado);
        storedVenta.setFacturado(publicVenta.facturado);
        return this.ventaRepository.save(storedVenta);
    }

    public Venta buildFromPublicVenta(PublicVenta publicVenta) {
        Venta aux = new Venta();
        aux.setCliente(this.clienteService.getById(publicVenta.cliente));
        aux.setPagado(publicVenta.pagado);
        try {
            aux.setFecha(Util.dateFromString(publicVenta.fecha));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        aux.setProductos(publicVenta.productos.stream().map(ProductoVenta::new).toList());
        return aux;
    }

    public List<PublicVenta> publicFromVentas(List<Venta> ventas) {
        return ventas
                .stream()
                .map(PublicVenta::new)
                .toList();
    }

    public Venta getById(int id) {
        return this.ventaRepository.getById((long) id);
    }

    public boolean isIdRegistered(int id) {
        return this.ventaRepository.countById((long) id) > 0;
    }

    @Transactional
    public void deleteById(int id) {
        Venta venta = this.getById(id);
        this.productoVentaRepository.deleteAll(venta.getProductos());
        this.ventaRepository.deleteById((long) id);
    }

    private Specification<Venta> addNombreClienteSpecification(String nombreCliente, Specification<Venta> spec) {
        if (nombreCliente != null && !nombreCliente.isEmpty()) {
            spec = spec.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("cliente").get("nombre")),
                            "%" + nombreCliente.toLowerCase() + "%"));
        }
        return spec;
    }

    private Specification<Venta> addFechaVentaSpecification(Date fechaVenta, Specification<Venta> spec) {
        if (fechaVenta != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("fecha"), fechaVenta));
        }
        return spec;
    }

    private Specification<Venta> addDaySpecification(Integer day, Specification<Venta> spec) {
        if (day != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(builder.function("day", Integer.class, root.get("fecha")), day));
        }
        return spec;
    }

    private Specification<Venta> addMonthSpecification(Integer month, Specification<Venta> spec) {
        if (month != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(builder.function("month", Integer.class, root.get("fecha")), month));
        }
        return spec;
    }

    private Specification<Venta> addYearSpecification(Integer year, Specification<Venta> spec) {
        if (year != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(builder.function("year", Integer.class, root.get("fecha")), year));
        }
        return spec;
    }
}
