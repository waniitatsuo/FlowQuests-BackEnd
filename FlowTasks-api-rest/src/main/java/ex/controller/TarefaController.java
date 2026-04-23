package ex.controller;

import ex.model.Tarefa;
import ex.model.Usuario;
import ex.model.repository.ConquistaRepository;
import ex.model.repository.TarefaRepository;
import ex.model.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

//Importações necessárias para Paginação e para o Enum 'Estado'
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ex.model.Tarefa.Estado;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tarefas")
@CrossOrigin("*")
public class TarefaController {
	
	// @Autowired faz a injeção de dependência: o Spring nos dá uma instância do Repository
	@Autowired
    private TarefaRepository repository; // Objeto para interagir com a tabela de tarefas no banco, e vice-versa

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConquistaRepository conquistaRepository;

    // Endpoint para criar uma nova tarefa (acessado via POST)
    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Tarefa novaTarefa) {

    	// Valida se o ID do usuário foi enviado na requisição
        if (novaTarefa.getUsuario() == null || novaTarefa.getUsuario().getId() == null) {
            return ResponseEntity.badRequest().body("O ID do usuário é obrigatório para criar uma tarefa.");
        }

        // Busca o objeto completo do usuário no banco de dados
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(novaTarefa.getUsuario().getId());
        if (usuarioOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuário com o ID fornecido não foi encontrado.");
        }

        // Associa a tarefa ao usuário real que veio do banco
        novaTarefa.setUsuario(usuarioOptional.get());
        novaTarefa.setId(null); // Garante que é uma inserção (ID nulo)
        novaTarefa.setEstado(Tarefa.Estado.pendente); // Garante que o estado inicial é pendente

        // Define a recompensa de XP com base na categoria da tarefa
        switch (novaTarefa.getCategoria()) {
            case remedios:
                novaTarefa.setRecompensaXp(70);
                break;
            case atividades:
                novaTarefa.setRecompensaXp(40);
                break;
            case trabalhos:
                novaTarefa.setRecompensaXp(50);
                break;
            case eventos:
                novaTarefa.setRecompensaXp(30);
                break;
            default:
                novaTarefa.setRecompensaXp(0);
        }
        
        // Salva a tarefa e retorna ela com status 201 (Criado)
        Tarefa tarefaSalva = repository.save(novaTarefa);
        return ResponseEntity.status(HttpStatus.CREATED).body(tarefaSalva);
    }

    @PostMapping("/{id}/completar")
    @Transactional
    public ResponseEntity<Usuario> completarTarefa(@PathVariable Long id) {
    	// Busca a tarefa pelo ID fornecido na URL
        Optional<Tarefa> tarefaOptional = repository.findById(id);
        if (tarefaOptional.isEmpty()) {
        	// Se não encontrar, retorna erro 404 (Not Found)
            return ResponseEntity.notFound().build();
        }

        Tarefa tarefa = tarefaOptional.get();
        if (tarefa.getEstado() == Tarefa.Estado.concluida) {
            return ResponseEntity.badRequest().build();
        }

        // Pega o usuário associado a esta tarefa
        Usuario usuario = tarefa.getUsuario();
        
        // Muda o estado da tarefa para "concluida"
        tarefa.setEstado(Tarefa.Estado.concluida);
        repository.save(tarefa);
        
        // Calcula e atualiza o XP total do usuário
        int novoXp = usuario.getXpTotal() + tarefa.getRecompensaXp();
        usuario.setXpTotal(novoXp);
        
        // Conta quantas tarefas o usuário já concluiu no total
        long tarefasConcluidas = repository.countByUsuarioIdAndEstado(usuario.getId(), Tarefa.Estado.concluida);

        // Lógica para dar a primeira conquista (ID 1, ou Conclua sua primeira tarefa!)
        if (tarefasConcluidas == 1) {
            conquistaRepository.findById(1L).ifPresent(conquista -> {
                usuario.getConquistas().add(conquista);
            });
        }

        // Lógica para dar a segunda conquista (ID 2, ou Conclua sua 10° tarefa!)
        if (tarefasConcluidas == 10) {
            conquistaRepository.findById(2L).ifPresent(conquista -> {
                usuario.getConquistas().add(conquista);
            });
        }
        
        // Salva as alterações no usuário (novo XP e possíveis novas conquistas)
        usuarioRepository.save(usuario);
        //repository.delete(tarefa); Não usamos mais para armazenar as tarefas antigas já feitas!
        
        
        Usuario usuarioDoBanco = usuarioRepository.findById(usuario.getId()).get();

        // Criamos uma "cópia limpa" do usuário para evitar erros de serialização
        Usuario respostaLimpa = new Usuario();
        respostaLimpa.setId(usuarioDoBanco.getId());
        respostaLimpa.setNome(usuarioDoBanco.getNome());
        respostaLimpa.setEmail(usuarioDoBanco.getEmail()); 
        respostaLimpa.setXpTotal(usuarioDoBanco.getXpTotal());
        respostaLimpa.setTarefas(new java.util.ArrayList<>(usuarioDoBanco.getTarefas())); 
        respostaLimpa.setConquistas(new java.util.HashSet<>(usuarioDoBanco.getConquistas()));
        
        // Retorna o objeto de usuário atualizado e limpo com status 200 (OK)
        return ResponseEntity.ok(respostaLimpa);
    }

    // Endpoint para listar todas as tarefas (geral)
    @GetMapping
    public List<Tarefa> listar() {
        return repository.findAll();
    }

    // Endpoint para listar tarefas de um usuário com filtro e paginação (acessado via GET)
    @GetMapping("/usuario/{usuarioId}")
    public Page<Tarefa> listarPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "pendente") String estado,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) { // 5 itens por página
        
    	// Cria um objeto de paginação (página atual, itens por página, e ordenação)
    	Pageable paging = PageRequest.of(page, size, Sort.by("dataPrazo").ascending().and(Sort.by("horaPrazo").ascending()));
        
        // Converte o texto do estado (ex: "pendente") para o tipo Enum correspondente
        Estado estadoEnum = Estado.valueOf(estado);
        
        // Executa a busca no repositório com todos os filtros
        return repository.findByUsuarioIdAndEstadoAndTituloContainingIgnoreCase(
            usuarioId, 
            estadoEnum, 
            search, 
            paging
        );
    }
    // Endpoint para atualizar uma tarefa
    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizar(@PathVariable Long id, @RequestBody Tarefa tarefaAtualizada) {
        return repository.findById(id)
                .map(tarefaExistente -> {
                    tarefaExistente.setTitulo(tarefaAtualizada.getTitulo());
                    tarefaExistente.setEstado(tarefaAtualizada.getEstado());
                    repository.save(tarefaExistente);
                    return ResponseEntity.ok(tarefaExistente);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint para deletar uma tarefa
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}