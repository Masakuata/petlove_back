package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.Pair;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Abono;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.entities.ProductoVenta;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.repositories.AbonoRepository;
import xatal.sharedz.repositories.ProductoVentaRepository;
import xatal.sharedz.repositories.VentaRepository;
import xatal.sharedz.structures.NewVenta;
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
    private final ProductoService productoService;

    public VentaService(
            VentaRepository ventaRepository,
            ProductoVentaRepository productoVentaRepository, AbonoRepository abonoRepository,
            ClienteService clienteService, ProductoService productoService) {
        this.ventaRepository = ventaRepository;
        this.productoVentaRepository = productoVentaRepository;
        this.abonoRepository = abonoRepository;
        this.clienteService = clienteService;
        this.productoService = productoService;
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

    public Venta saveVenta(NewVenta newVenta) {
        Pair<Venta, Float> pair = this.buildFromNewVenta(newVenta);
        Venta ventaToSave = pair.a;
        float abonoInicial = pair.b;

        int tipoCliente = ventaToSave.getCliente().getTipoCliente();
        float costoTotal = this.getCostoTotalByVentaAndTipoCliente(ventaToSave);
        ventaToSave.setPagado(abonoInicial >= costoTotal);

        Venta savedVenta = this.saveVenta(ventaToSave);
        this.saveAbono(new Abono(savedVenta.getId().intValue(), abonoInicial, new Date()));
        this.ventaRepository.save(savedVenta);
        this.updateStocks(savedVenta);
        return savedVenta;
    }

    public Venta saveVenta(Venta venta) {
        List<ProductoVenta> productosGuardados = new LinkedList<>();
        this.productoVentaRepository.saveAll(venta.getProductos())
                .forEach(productosGuardados::add);
        venta.setProductos(productosGuardados);
        return this.ventaRepository.save(venta);
    }

    public Abono saveAbono(Abono abono) {
        return this.abonoRepository.save(abono);
    }

    public Abono saveAbono(PublicAbono newAbono) {
        Abono savedAbono = this.saveAbono(new Abono(newAbono));
        Venta venta = this.ventaRepository.getById((long) newAbono.venta);
        float costo = this.getCostoTotalByVentaAndTipoCliente(venta);
        float abonos = this.getTotalAbonosByVenta(Math.toIntExact(venta.getId()));

        venta.setPagado(abonos >= costo);
        this.ventaRepository.save(venta);

        return savedAbono;
    }

    public List<Abono> getAbonos(int idVenta) {
        return this.abonoRepository.findByVenta((long) idVenta);
    }

    public boolean isAbonoRegistered(int idAbono) {
        return this.abonoRepository.countById((long) idAbono) > 0;
    }

    public float getTotalAbonosByVenta(int ventaId) {
        return this.abonoRepository.sumAbonosByVenta((long) ventaId);
    }

    private List<Producto> getProductosByVentaAndTipoCliente(Venta venta) {
        List<Integer> productosId = venta.getProductos()
                .stream()
                .map(productoVenta -> productoVenta.getProducto().intValue())
                .toList();
        return this.productoService
                .searchByIdAndTipoCliente(productosId, venta.getCliente().getTipoCliente());
    }

    private float getCostoTotalByVentaAndTipoCliente(Venta venta) {
        return this.getProductosByVentaAndTipoCliente(venta)
                .stream()
                .reduce(0f, (precio, e) -> precio + e.getPrecio(), Float::sum);
    }

    private void updateStocks(Venta venta) {
        this.productoService.updateStockFromVenta(venta);
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

    public Pair<Venta, Float> buildFromNewVenta(NewVenta newVenta) {
        return new Pair<>(this.buildFromPublicVenta(newVenta), newVenta.abono);
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
