package com.battleship.battleship.model;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The Board class implements the game board and logic for Blanket Fort Water Fight.
 * It is to be used with the Cell class.
 */
public class Board {
    private long id;

    private final int ROWS = 10;
    private final int COLUMNS = 10;

    private final int[] SCORE_TABLE = {0, 1, 2, 5, 10, 20};
    private final int MAX_SCORE = 3000;
    private int currentScore = 0;
    private boolean gameEnded = false;
    public int[] lastOpponentPoints = new int[0];

    private List<List<Cell>> fortMap;
    private List<List<int[]>> fortLocations;
    private int numActiveOpponentForts;

    private boolean isCheating = false;

    private boolean isValidPosition(int x, int y) {
        return x >= 0 && x < ROWS && y >= 0 && y < COLUMNS;
    }

    private void fortCleanUp(List<int[]> placedTiles) {
        // Stream 1
        placedTiles.forEach(pair -> {
            int x = pair[0];
            int y = pair[1];
            this.fortMap.get(x).get(y).setContent(' ');
        });
    }

    public Cell boardAt(int x, int y) {
        return fortMap.get(x).get(y);
    }

    public Board(int numEnemies) {
        numActiveOpponentForts = numEnemies;
        fillBoard(); //fills fortMap with a 10x10 List<List<Cell>>.
        fortLocations = new ArrayList<>();

        final int MAX_FAIL_ATTEMPTS = 500;
        int failedAttempts = 0;
        boolean fortPlaced = false;
        for (char fortMarker = 'A'; fortMarker < 'A' + numEnemies; fortMarker++) {
            while (!fortPlaced) {
                try {
                    placeFort(fortMarker, 0);
                    fortPlaced = true;
                } catch (RuntimeException e) {
                    // caught the placeFort not able to place fort, so do it again
                    failedAttempts++;
                }
                if (failedAttempts == MAX_FAIL_ATTEMPTS) {
                    failedAttempts = 0;
                    fillBoard();
                    this.fortLocations = new ArrayList<>();
                    fortMarker = 'A';
                }
            }
            fortPlaced = false;
        }
    }

    private void fillBoard() {
        fortMap = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            List<Cell> rowFort = new ArrayList<>();
            for (int j = 0; j < COLUMNS; j++) {
                rowFort.add(new Cell());
            }
            fortMap.add(rowFort);
        }
    }

    private void placeFort(char fortMarker, int iterationCalled) {
        final int MAX_ALLOWED_ITERATION = 10;
        if (iterationCalled > MAX_ALLOWED_ITERATION) {
            throw new RuntimeException();
        }
        Random random = new Random();
        List<int[]> placedTiles = new ArrayList<>();
        final List<String> directions = new ArrayList<>();
        directions.add("left");
        directions.add("right");
        directions.add("up");
        directions.add("down");

        int x;
        int y;
        do {
            x = random.nextInt(ROWS);
            y = random.nextInt(COLUMNS);
        } while (boardAt(x, y).hasFort());
        boardAt(x, y).setContent(fortMarker);
        placedTiles.add(new int[]{x, y});

        final int FORT_LENGTH = 5;
        for (int i = 1; i < FORT_LENGTH; i++) {
            Collections.shuffle(directions);
            int testX;
            int testY;
            int numTries = 0;
            final int MAX_TRIES = 4;
            do {
                if (numTries == MAX_TRIES) {
                    fortCleanUp(placedTiles);
                    placeFort(fortMarker, iterationCalled + 1);
                    return;
                }
                testX = x;
                testY = y;
                String direction = directions.get(numTries);
                switch (direction) {
                    case "left":
                        testY--;
                        break;
                    case "right":
                        testY++;
                        break;
                    case "up":
                        testX--;
                        break;
                    case "down":
                        testX++;
                        break;
                }
                numTries++;
            } while (!isValidPosition(testX, testY) || boardAt(testX, testY).hasFort());
            x = testX;
            y = testY;
            boardAt(x, y).setContent(fortMarker);
            placedTiles.add(new int[]{x, y});
        }
        fortLocations.add(placedTiles);
    }

    public void hitsTile(String tileToHit) {
        int rowNum = Character.toUpperCase(tileToHit.charAt(0)) - 'A';
        int colNum = Integer.parseInt(tileToHit.substring(1)) - 1;
        boardAt(rowNum, colNum).getsHit();
        if (boardAt(rowNum, colNum).hasFort()) {
            char fortMarker = boardAt(rowNum, colNum).getContent();
            boardAt(rowNum, colNum).setContent(Character.toLowerCase(fortMarker));
        }

        int[] hitCoordinate = new int[]{rowNum, colNum};
        // Stream 2
        fortLocations
                .forEach(fortLocation ->
                        fortLocation.removeIf(coordinate -> Arrays.equals(coordinate, hitCoordinate)));

        // Stream 3
        AtomicInteger index = new AtomicInteger(0);
        lastOpponentPoints = new int[fortLocations.size()];
        currentScore += fortLocations.stream()
                .mapToInt(list -> {
                    int score = SCORE_TABLE[list.size()];
                    lastOpponentPoints[index.getAndIncrement()] = score;
                    return score;
                })
                .sum();

        gameEnded = currentScore >= MAX_SCORE || gameWon();
        numActiveOpponentForts = 0;
        for (List<int[]> fort: fortLocations) {
            if (!fort.isEmpty()) {
                numActiveOpponentForts++;
            }
        }
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public boolean gameEnded() {
        return gameEnded;
    }

    public boolean gameWon() {
        if (getCurrentScore() >= MAX_SCORE) {
            return false;
        }
        for (List<int[]> fortLocation : fortLocations) {
            if (!fortLocation.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int getROWS() {
        return ROWS;
    }

    public int getCOLUMNS() {
        return COLUMNS;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int[] getLastOpponentPoints() {
        return lastOpponentPoints;
    }

    public int getMAX_SCORE() {
        return MAX_SCORE;
    }

    public boolean isCheating() {
        return isCheating;
    }

    public void setCheating(boolean cheating) {
        isCheating = cheating;
    }

    public int getNumActiveOpponentForts() {
        return numActiveOpponentForts;
    }
}
