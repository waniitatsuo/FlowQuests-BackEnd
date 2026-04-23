package ex.repository;

import ex.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    // Novo método mágico para o Ranking! (Traz todos ordenados do maior XP pro menor)
    List<Usuario> findAllByOrderByXpTotalDesc();
}