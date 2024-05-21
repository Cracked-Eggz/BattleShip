package com.battleship.battleship.ApiGameDTO;

import com.battleship.battleship.model.Board;

/**
 * DTO class for the REST API to define object structures required by the front-end.
 * HINT: Create static factory methods (or constructors) which help create this object
 *       from the data stored in the model, or required by the model.
 */
public class ApiBoardDTO {
    public int boardWidth;
    public int boardHeight;

    // celState[row][col] = {"fog", "hit", "fort", "miss", "field"}
    public String[][] cellStates;

    public static ApiBoardDTO create(Board board) {
        return new ApiBoardDTO(board);
    }

    public ApiBoardDTO (Board board){
        this.boardWidth = board.getCOLUMNS();
        this.boardHeight = board.getROWS();
        this.cellStates = new String[boardHeight][boardWidth];
        for (int i = 0; i < boardWidth; i++) {
            for (int j = 0; j < boardHeight; j++) {
                this.cellStates[i][j] = board.boardAt(i,j).cellState(board.isCheating());
            }
        }
    }
}
