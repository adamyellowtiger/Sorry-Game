public static void initBaseGrid() {
    // Fill everything with empty spaces to ensure alignment
    for (int r = 0; r < SIZE; r++) {
        for (int c = 0; c < SIZE; c++) {
            baseGrid[r][c] = "  ";
        }
    }

    /*
    Fill in the perimeter by assigning each grid along the perimeter with its corresponding index number
    */
    for (int i = 0; i < TRACK_LEN; i++) {
        int r = trackRow[i];
        int c = trackColumn[i];
        baseGrid[r][c] = twoDigits(i);
    }

    // Define the start and home stretch spaces for each player
    // Example for one player
    baseGrid[13][1] = "SA"; // Start area
    baseGrid[13][2] = "FE"; // Few spaces leading to track
    baseGrid[13][3] = "ZO"; // Zone
    baseGrid[13][4] = "NE"; // Near stretch
    baseGrid[13][5] = "!!"; // Home stretch start
    baseGrid[13][6] = "ST"; // Home stretch end

    baseGrid[9][13] = "ST"; // Another player's start
    baseGrid[10][13] = "SA";
    baseGrid[11][13] = "FE";
    baseGrid[12][13] = "ZO";
    baseGrid[13][13] = "NE";
    baseGrid[14][13] = "!!";

    // Add home stretches to the grid for all players
    // Modify these coordinates as necessary to reflect all players
    baseGrid[14][1] = "HM"; // Home start example
    baseGrid[14][2] = "HM";
    baseGrid[14][3] = "HM";
    baseGrid[14][4] = "HM";
}