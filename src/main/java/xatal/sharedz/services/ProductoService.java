package xatal.sharedz.services;

import org.springframework.stereotype.Service;
import xatal.sharedz.entities.Producto;
import xatal.sharedz.repositories.ProductoRepository;
import xatal.sharedz.structures.NewProducto;

import java.util.List;

@Service
public class ProductoService {
    private final ProductoRepository productos;

    public ProductoService(ProductoRepository productos) {
        this.productos = productos;
    }

    public List<Producto> getAll() {
        return this.productos.getAll();
    }

    public Producto newProducto(NewProducto newProducto) {
        return this.productos.save(new Producto(newProducto));
    }

    public boolean isIdRegistered(int idProducto) {
        return this.productos.countById(idProducto) > 0;
    }

    public boolean deleteById(int idProducto) {
        this.productos.deleteById(idProducto);
        return true;
        // TODO Check if producto is used on another table
    }
}
