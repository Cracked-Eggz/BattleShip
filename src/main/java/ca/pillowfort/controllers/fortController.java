package ca.pillowfort.controllers;

import ca.pillowfort.ApiGameDTO.*;
import ca.pillowfort.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class fortController {
    private List<Board> games = new ArrayList<>();
    private AtomicLong nextId = new AtomicLong();

    @GetMapping("/api/about")
    public String getAuthorName() {
        return "陈亮 + 陈登";
    }

    @GetMapping("/api/games")
    public List<ApiGameDTO> getAllGames() {
        List<ApiGameDTO> ApiGameDTOs = new ArrayList<>();
        for (Board game : games) {
            ApiGameDTOs.add(new ApiGameDTO(game));
        }
        return ApiGameDTOs;
    }

    @PostMapping("/api/games")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ApiGameDTO> createNewGame() {
        Board game = new Board(5); // Assuming you're creating a new Board with a default size
        game.setId(nextId.incrementAndGet());

        games.add(game);
        ApiGameDTO apiGameDTO = new ApiGameDTO(game);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiGameDTO);
    }

    @GetMapping("/api/games/{id}")
    public ApiGameDTO getGame(@PathVariable("id") long gameId) {
        if (gameId > games.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        for (Board game : games) {
            if (game.getId() == gameId) {
                return new ApiGameDTO(game);
            }
        }
        throw new IllegalArgumentException();
    }

    @GetMapping("/api/games/{id}/board")
    public ApiBoardDTO getOneBoard(@PathVariable("id") long gameId) {
        if (gameId > games.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found");
        }

        for (Board game : games) {
            if (game.getId() == gameId) {
                return new ApiBoardDTO(game);
            }
        }
        throw new IllegalArgumentException();
    }

    @PostMapping("/api/games/{id}/moves")
    public ResponseEntity<?> makeMove(@PathVariable("id") long gameId, @RequestBody ApiLocationDTO apiLocationDTO) {
        if (gameId > games.size()) {
            return ResponseEntity.notFound().build();
        } else if (isInvalidLocation(apiLocationDTO)) {
            return ResponseEntity.badRequest().body("Invalid?");
        }

        for (Board game : games) {
            if (game.getId() == gameId) {
                // string format of Alphabet + Int (ex., A1)
                String location = String.valueOf((char)(apiLocationDTO.getRow() + 65))
                        + (apiLocationDTO.getCol() + 1);
                game.hitsTile(location);
                return ResponseEntity.accepted().build();
            }
        }
        return ResponseEntity.badRequest().body("ended?");
    }

    private boolean isInvalidLocation(ApiLocationDTO locationDTO) {
        int row = locationDTO.getRow();
        int col = locationDTO.getCol();
        return row < 0 || row > 9 || col < 0 || col > 9;
    }

    @PostMapping("/api/games/{id}/cheatstate")
    public ResponseEntity<?> cheat(@PathVariable("id") long gameId, @RequestBody String cheatString) {
        if (gameId > games.size()) {
            return ResponseEntity.notFound().build();
        } else if (!cheatString.equals("SHOW_ALL")) {
            return ResponseEntity.badRequest().build();
        }

        for (Board game : games) {
            if (game.getId() == gameId) {
                game.setCheating(true);
                return ResponseEntity.status(HttpStatus.ACCEPTED).build();
            }
        }
        return ResponseEntity.notFound().build();
    }

}
