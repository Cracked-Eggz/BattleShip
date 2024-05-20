package ca.pillowfort.ApiGameDTO;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO class for the REST API to define object structures required by the front-end.
 * HINT: Create static factory methods (or constructors) which help create this object
 *       from the data stored in the model, or required by the model.
 */
public class ApiLocationDTO {
    @JsonProperty("row")
    public int row;

    @JsonProperty("col")
    public int col;

    public static ApiLocationDTO create(int row, int col) {
        return new ApiLocationDTO(row, col);
    }

    public ApiLocationDTO(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
