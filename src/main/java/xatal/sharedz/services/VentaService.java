package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Abono;
import xatal.sharedz.entities.ProductoVenta;
import xatal.sharedz.entities.Venta;
import xatal.sharedz.repositories.AbonoRepository;
import xatal.sharedz.repositories.ProductoVentaRepository;
import xatal.sharedz.repositories.VentaRepository;
import xatal.sharedz.structures.Attachment;
import xatal.sharedz.structures.PublicAbono;
import xatal.sharedz.structures.PublicVenta;
import xatal.sharedz.util.Util;
import xatal.sharedz.util.XEmail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

@Service
public class VentaService {
    private final Logger logger = LoggerFactory.getLogger(VentaService.class);
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

    public List<Venta> searchVentas(String nombreCliente, Date fechaVenta) {
        Specification<Venta> spec = Specification.where(null);
        if (nombreCliente != null && !nombreCliente.isEmpty()) {
            spec = spec.and(((root, query, builder) ->
                    builder.like(builder.lower(root.get("cliente").get("nombre")),
                            "%" + nombreCliente.toLowerCase() + "%")));
        }
        if (fechaVenta != null) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("fecha"), fechaVenta));
        }
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
        return this.abonoRepository.countById(Long.valueOf(idAbono)) > 0;
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
        return this.ventaRepository.getById((long) id).orElse(null);
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

    public String dateCSV(Date ventaDate) {
        String path;
        try {
            path = "ventas.csv";
            List<Venta> ventas = this.searchVentas(null, ventaDate);
            BufferedWriter writer = new BufferedWriter(new FileWriter(path));
            writer.write("ID VENTA,CLIENTE,PAGADO,FECHA,FACTURADO\n");
            for (Venta venta : ventas) {
                StringBuilder message = new StringBuilder();
                message.append(venta.getId()).append(",");
                message.append(venta.getCliente().getNombre()).append(",");
                if (venta.isPagado()) {
                    message.append("SI").append(",");
                } else {
                    message.append("NO").append(",");
                }
                message.append(new SimpleDateFormat("dd-MM-yyyy").format(venta.getFecha())).append(",");
                if (venta.isFacturado()) {
                    message.append("SI").append(",");
                } else {
                    message.append("NO").append(",");
                }
                message.append("\n");
                writer.write(message.toString());
            }
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    public void sendCSV(Date ventaDate, String recipientName, String recipientEmail) {
        String csvPath = this.dateCSV(ventaDate);
        if (csvPath != null && !csvPath.isEmpty()) {
            String stringDate = new SimpleDateFormat("dd-MM-yyyy").format(ventaDate);
            XEmail email = new XEmail();
            email.setFrom("edsonmanuelcarballovera@gmail.com");
            email.setSubject("Ventas del dia " + stringDate);
            email.addRecipient(recipientName, recipientEmail);
            email.setMessage("A continuacion encontrara adjunto las ventas del dia " + stringDate);
            try {
                email.addAttachment(
                        csvPath,
                        Files.readAllBytes(new File(csvPath).toPath()),
                        "text/csv"
                );
            } catch (IOException e) {
                this.logger.error(e.getMessage());
            }
            email.send();
        }
    }
}
