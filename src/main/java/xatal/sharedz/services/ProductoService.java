package xatal.sharedz.services;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.repositories.ProductoRepository;
import xatal.sharedz.structures.PublicProducto;
import xatal.sharedz.util.Util;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {
    private final ProductoRepository productos;

    private List<Producto> productosCache = null;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    public List<Producto> getAll() {
        if (this.productosCache == null) {
            this.productosCache = this.productos.getAll();
        }
        return this.productosCache;
    }

    public List<Producto> searchByName(String nombre) {
        if (this.productosCache == null) {
            this.productosCache = this.productos.getAll();
        }
        return this.productosCache
                .stream()
                .filter(producto ->
                        Util.containsAnyCase(producto.getNombre(), nombre))
                .collect(Collectors.toList());
    }

    public Producto newProducto(PublicProducto newProducto) {
        this.productosCache = null;
        return this.productos.save(new Producto(newProducto));
    }

    public boolean isIdRegistered(int idProducto) {
        return this.productos.countById(idProducto) > 0;
    }

    @Transactional
    public boolean deleteById(int idProducto) {
        this.productosCache = null;
        this.productos.deleteById(idProducto);
        return true;
        // TODO Check if producto is used on another table
    }
}
