package ca.pillowfort.model;

/**
 * The cell class keeps track of the current state of each cell in a 2D array.
 * It keeps track of whether each cell isHit,hasFort and content within each cell.
 * This method is to be used with the Board class
 */
public class Cell {
    private boolean isHit;
    private boolean hasFort;
    private char content;

    public Cell() {
        isHit = false;
        hasFort = false;
        content = ' ';
    }

    public boolean isHit() {
        return isHit;
    }

    public void getsHit() {
        isHit = true;
    }

    public boolean hasFort() {
        return hasFort;
    }

    public char getContent() {
        return content;
    }

    public void setContent(char content) {
        this.content = content;
        this.hasFort = (content != ' ');
    }

    public String cellState(boolean isCheating) {
        if (isCheating && !isHit) {
            if (hasFort) {
                return "fort";
            } else {
                return "field";
            }
        } else if (isHit){
            if (hasFort) {
                return "hit";
            } else {
                return "miss";
            }
        }
        return "fog";
    }
}
