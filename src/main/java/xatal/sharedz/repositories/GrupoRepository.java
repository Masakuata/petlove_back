package xatal.sharedz.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xatal.sharedz.entities.Grupo;

import java.util.Optional;

@Repository
public interface GrupoRepository extends CrudRepository<Grupo, Long> {
    Optional<Grupo> getGrupoByName(String name);

    Long countByName(String name);
}
