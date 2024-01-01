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
        String lowercase = nombre.toLowerCase();
        return this.productos.getAll()
                .stream()
                .filter(producto -> producto.getNombre().toLowerCase().contains(lowercase))
                .collect(Collectors.toList());
    }

    public List<Producto> searchByNameAndTipoCliente(String nombre, int tipoCliente) {
        List<Producto> productos = this.searchByName(nombre);
        if (productos.isEmpty()) {
            return Collections.emptyList();
        }
        List<Precio> precios = this.precioRepository.findByProductoInAndCliente(
                productos.stream().map(Producto::getId).collect(Collectors.toList()),
                (long) tipoCliente
        );
        productos.forEach(producto -> {
            Optional<Precio> currentPrecio = precios.stream()
                    .filter(precio -> Objects.equals(precio.getProducto(), producto.getId()))
                    .findFirst();
            currentPrecio.ifPresent(precio -> producto.setPrecio(precio.getPrecio()));
        });
        return productos;
    }

    public Optional<Producto> getByIdAndTipoCliente(int idProducto, int tipoCliente) {
        Optional<Producto> producto = this.getProductoById(idProducto);
        producto.ifPresent(value -> this.setProductPrice(value, tipoCliente));
        return producto;
    }

    private Optional<Producto> getProductoById(int idProducto) {
        return this.getAll()
                .stream()
                .filter(producto -> producto.getId() == idProducto)
                .findFirst();
    }

    private void setProductPrice(Producto producto, int tipoCliente) {
        this.precioRepository
                .findByProductoAndCliente(producto.getId(), (long) tipoCliente)
                .ifPresent(precio -> producto.setPrecio(precio.getPrecio()));
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
