package xatal.petlove.services;

import org.springframework.stereotype.Service;
import xatal.petlove.entities.Abono;
import xatal.petlove.entities.Venta;
import xatal.petlove.repositories.AbonoRepository;
import xatal.petlove.structures.NewAbono;
import xatal.petlove.structures.PublicAbono;

import java.util.List;
import java.util.Optional;

@Service
public class AbonoService {
	private final AbonoRepository abonoRepository;
	private final SearchVentaService searchVentaService;
	private final VentaService ventaService;

	public AbonoService(AbonoRepository abonoRepository, SearchVentaService searchVentaService, VentaService ventaService) {
		this.abonoRepository = abonoRepository;
		this.searchVentaService = searchVentaService;
		this.ventaService = ventaService;
	}


	public Abono storeAbono(Abono abono) {
		return this.abonoRepository.save(abono);
	}

	public Optional<Abono> saveNewAbono(NewAbono newAbono) {
		Optional<Venta> optionalVenta = this.searchVentaService.getById(newAbono.venta);
		if (optionalVenta.isEmpty()) {
			return Optional.empty();
		}
		Venta venta = optionalVenta.get();
		if (newAbono.finiquito) {
			newAbono.cantidad = venta.getTotal() - venta.getAbonado();
			venta.setPagado(true);
		} else {
			this.ventaService.setVentaPagado(venta);
		}
		venta.setAbonado(venta.getAbonado() + newAbono.cantidad);
		Abono savedAbono = this.abonoRepository.save(new Abono(newAbono));
		this.ventaService.storeVenta(venta);
		return Optional.of(savedAbono);
	}

	public Abono updateAbono(PublicAbono publicAbono, long idAbono) {
		this.abonoRepository.updateAbonoCantidad(idAbono, publicAbono.cantidad);
		Optional<Venta> optionalVenta = this.searchVentaService.getById(publicAbono.venta);
		optionalVenta.ifPresent(venta -> {
			venta.setAbonado(this.abonoRepository.sumAbonosByVenta(venta.getId()));
			this.ventaService.storeVenta(venta);
		});
		Abono abono = new Abono(publicAbono);
		abono.setId(idAbono);
		return abono;
	}

	public List<Abono> getAbonosFromVentaId(long idVenta) {
		return this.abonoRepository.findByVenta(idVenta);
	}

	public boolean isAbonoRegistered(long idAbono) {
		return this.abonoRepository.countById(idAbono) > 0;
	}

	public float getTotalAbonosByVentaId(long idVenta) {
		return this.abonoRepository.sumAbonosByVenta(idVenta);
	}

	public void deleteAbonosByVentaId(long idVenta) {
		this.abonoRepository.deleteAbonosByVenta(idVenta);
	}
}
