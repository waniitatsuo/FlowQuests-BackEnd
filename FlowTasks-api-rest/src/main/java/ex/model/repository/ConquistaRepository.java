package ex.repository;

import ex.model.Conquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConquistaRepository extends JpaRepository<Conquista, Long> {
    
    // Busca conquistas que o usuário ainda não tem, mas que já atingiu o XP
    // (Útil para processos de verificação automática)
    List<Conquista> findByXpNecessarioLessThanEqual(int xp);
}