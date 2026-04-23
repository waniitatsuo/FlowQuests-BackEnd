package ex.model.repository;

import ex.model.Conquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConquistaRepository extends JpaRepository<Conquista, Long> {
	
}