package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Precio;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.repositories.PrecioRepository;
import xatal.sharedz.repositories.ProductoRepository;
import xatal.sharedz.repositories.ProductoVentaRepository;
import xatal.sharedz.structures.PublicPrecio;
import xatal.sharedz.structures.PublicProducto;
import xatal.sharedz.util.Util;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    private final ProductoRepository productos;
    private final ProductoVentaRepository productoVenta;
    private final PrecioRepository precioRepository;

    private List<Producto> productosCache = null;

    public ProductoService(ProductoRepository productos, ProductoVentaRepository productoVenta, PrecioRepository precioRepository) {
        this.productos = productos;
        this.productoVenta = productoVenta;
        this.precioRepository = precioRepository;
    }

    public List<Producto> getAll() {
        if (this.productosCache == null) {
            this.productosCache = this.productos.getAll();
        }
        return this.productosCache;
    }

    public List<Producto> searchByName(String nombre) {
        return this.productos.getAll()
                .stream()
                .filter(producto ->
                        Util.containsAnyCase(producto.getNombre(), nombre))
                .collect(Collectors.toList());
    }

    public List<Producto> searchByNameAndCliente(String nombre, int idCliente) {
        List<Producto> productos = this.searchByName(nombre);
        if (productos.isEmpty()) {
            return Collections.emptyList();
        }
        List<Precio> precios = this.precioRepository.findByProductoInAndCliente(
                productos.stream().map(Producto::getId).collect(Collectors.toList()),
                (long) idCliente
        );
        productos.forEach(producto -> {
            Optional<Precio> currentPrecio = precios.stream()
                    .filter(precio -> Objects.equals(precio.getProducto(), producto.getId()))
                    .findFirst();
            currentPrecio.ifPresent(precio -> producto.setPrecio(precio.getPrecio()));
        });
        return productos;
    }

    public boolean setPreciosById(int idProducto, List<PublicPrecio> newPrecios) {
        if (!this.isIdRegistered(idProducto)) {
            return false;
        }

        List<Precio> preciosToSave = this.precioRepository.findByProducto((long) idProducto);
        newPrecios.forEach(newPrecio -> {
            Optional<Precio> precioOptional = preciosToSave
                    .stream()
                    .filter(x -> x.getCliente().intValue() == newPrecio.tipoCliente)
                    .findFirst();
            if (precioOptional.isPresent()) {
                precioOptional.get().setPrecio(newPrecio.precio);
            } else {
                Precio aux = new Precio();
                aux.setProducto((long) idProducto);
                aux.setCliente((long) newPrecio.tipoCliente);
                aux.setPrecio(newPrecio.precio);
                preciosToSave.add(aux);
            }
        });
        this.precioRepository.saveAll(preciosToSave);
        return true;
    }

    private Precio publicToPrecio(PublicPrecio publicPrecio, Long productoId) {
        Precio precio = new Precio();
        precio.setProducto(productoId);
        precio.setCliente((long) publicPrecio.tipoCliente);
        precio.setPrecio(publicPrecio.precio);
        return precio;
    }

    public Producto newProducto(PublicProducto newProducto) {
        this.productosCache = null;
        return this.productos.save(new Producto(newProducto));
    }

    public boolean isIdRegistered(int idProducto) {
        return this.productos.countById(idProducto) > 0;
    }

    public boolean isUsed(int idProducto) {
        return this.productoVenta.countByProducto((long) idProducto) > 0;
    }

    @Transactional
    public boolean deleteById(int idProducto) {
        if (!this.isUsed(idProducto)) {
            this.productosCache = null;
            this.productos.deleteById(idProducto);
            return true;
        }
        return false;
        // TODO Check if producto is used on another table
    }
}
