package xatal.sharedz.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import xatal.sharedz.structures.PublicAbono;
import xatal.sharedz.util.Util;

import java.text.ParseException;
import java.util.Date;

@Entity
public class Abono {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "venta")
    private Long venta;

    @Column(name = "cantidad")
    private float cantidad;

    @Column(name = "fecha")
    private Date fecha;

    public Abono() {
    }

    public Abono(PublicAbono abono) {
        this.venta = (long) abono.venta;
        this.cantidad = abono.cantidad;
        try {
            this.fecha = Util.dateFromString(abono.fecha);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getVenta() {
        return venta;
    }

    public void setVenta(Long venta) {
        this.venta = venta;
    }

    public float getCantidad() {
        return cantidad;
    }

    public void setCantidad(float cantidad) {
        this.cantidad = cantidad;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}
