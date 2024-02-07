package xatal.petlove.repositories;

import org.springframework.data.repository.CrudRepository;
import xatal.petlove.entities.Operation;

public interface OperationRepository extends CrudRepository<Operation, Long> {
}
