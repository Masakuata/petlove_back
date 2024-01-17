package xatal.petlove.repositories;

import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.StockOperation;

public interface StockOperationRepository extends CrudRepository<StockOperation, Long> {
}
