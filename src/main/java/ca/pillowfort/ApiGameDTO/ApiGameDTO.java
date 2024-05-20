package ca.pillowfort.ApiGameDTO;

import ca.pillowfort.model.Board;

import java.util.List;

/**
 * DTO class for the REST API to define object structures required by the front-end.
 * HINT: Create static factory methods (or constructors) which help create this object
 *       from the data stored in the model, or required by the model.
 */
public class ApiGameDTO {
    public long gameNumber;
    public boolean isGameWon;
    public boolean isGameLost;
    public int opponentPoints;
    public int maxPoints;
    public long numActiveOpponentForts;

    // Number of points that the opponents scored on the last time they fired.
    // If opponents have not yet fired, then it should be an empty array (0 size).
    public int[] lastOpponentPoints;

    public static ApiGameDTO create(Board board) {
        return new ApiGameDTO(board);
    }

    public ApiGameDTO(Board board) {
        this.gameNumber = board.getId();
        this.isGameWon = board.gameWon();
        this.isGameLost = board.gameEnded() && !board.gameWon();
        this.opponentPoints = board.getCurrentScore();
        this.maxPoints = board.getMAX_SCORE();
        this.numActiveOpponentForts = board.getNumActiveOpponentForts();
        this.lastOpponentPoints = board.getLastOpponentPoints();
    }
}
