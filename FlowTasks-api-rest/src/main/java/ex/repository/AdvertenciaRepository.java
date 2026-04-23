package ex.repository;

import ex.model.Advertencia;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdvertenciaRepository extends JpaRepository<Advertencia, Long> {
    
    // O Spring faz a mágica de contar no banco só pelo nome do método!
    long countByUsuarioId(Long usuarioId);
}