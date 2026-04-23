package ex.repository;

import ex.model.Tarefa;
import ex.model.Tarefa.Estado; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page; 
import org.springframework.data.domain.Pageable;
import java.util.List;


@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Long> {
	
    List<Tarefa> findByUsuarioId(Long usuarioId);
    
    long countByUsuarioIdAndEstado(Long usuarioId, Tarefa.Estado estado);
    
    @Query("SELECT t FROM Tarefa t WHERE t.usuario.id = :usuarioId AND t.estado = :estado AND lower(t.titulo) LIKE lower(concat('%', :search, '%'))")
    Page<Tarefa> findByUsuarioIdAndEstadoAndTituloContainingIgnoreCase(
        @Param("usuarioId") Long usuarioId, 
        @Param("estado") Estado estado, 
        @Param("search") String search, 
        Pageable pageable
    );
}