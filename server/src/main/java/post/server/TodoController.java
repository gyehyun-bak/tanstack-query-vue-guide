package post.server;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todos")
@CrossOrigin(origins = "http://localhost:5173")
public class TodoController {
    private final TodoRepository todoRepository;

    @GetMapping
    public ResponseEntity<List<Todo>> getPosts() {
        return ResponseEntity.ok(todoRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Todo> createPost(@RequestBody CreateTodoRequest request) {
        Todo todo = new Todo();
        todo.setTitle(request.getTitle());
        return ResponseEntity.ok(todoRepository.save(todo));
    }
}
