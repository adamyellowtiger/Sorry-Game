import java.util.Arrays;
import java.util.Base64;

/**
 * @author Adam Fan
 */


public class Sorry {
    static final int PLAYERS = 4;
    static final int SIZE = 16;
    static final int TRACK_LEN = 60;
    static final int PAWNS_PER_PLAYER = 4;
    static final int HOME_STRETCH_SIZE = 5;
    static final String[] PLAYER_COLORS = {"Red", "Blue", "Yellow", "Green"};
    static int[][] pawnPositions = new int[PLAYERS][PAWNS_PER_PLAYER];
    static int[] savedSorryCards = new int[PLAYERS];
    static int currentPlayer = 0;
    static int turnCount = 0;

    public static void displayRules() {
        System.out.println("\n=== SORRY! RULES ===");
        System.out.println("Goal: Move all 4 pawns from Start to Home.");
        System.out.println("Draw cards to move. Land on opponents to send them back to Start!");
        System.out.println("Cards: 1,2 = Start or move | 3,5,8,12 = Forward | 4 = Backward");
        System.out.println("       7 = Split move | 10 = Forward 10 or Back 1 | 11 = Switch");
        System.out.println("       Sorry! = Bump opponent with pawn from Start");
        System.out.println("====================\n");
    }
    static int[] trackRow = new int[TRACK_LEN];
    static int[] trackColumn = new int[TRACK_LEN];

    // Each cell stores TWO characters of content, printed as [XY].
    // Example: [07], [59], or [  ] for empty.
    static String[][] baseGrid = new String[SIZE][SIZE];


    //Step 1.1: Build the 60-space track mapping (perimeter of a 16x16 square is coincidentally 60, since 4*(16-1) = 60)
    public static void initTrack() {
        /*
        This method initializes the trackRow and trackColumn arrays with the indices of the track segments.
        This is done through making putting each square's row and column number into the corresponding index
        of the two arrays.
        For example, the square one is marked by (0,0), so trackRow[0] = 0 and trackColumn[0] = 0.
        Another example is that square 30 is marked by (15,15), so trackRow[30] = 15 and trackColumn[30] = 15.
        A diagram is offered below for visualization purposes.
         */
        /*
        --------------------------------------
        |                                    |
        |                                    |
        |                                    |
        |                                    |
        |                                    |
        |                                    |
        |                                    |
        |                                    |
        |                                    |
        |                                    |
        --------------------------------------
         */
        int index = 0;

        // Top edge: (0,0) to (0,15)

        for (int c = 0; c < SIZE; c++) {
            trackRow[index] = 0;
            trackColumn[index] = c;
            index++;
        }

        // Right edge: (1,15) to (15,15)
        for (int r = 1; r < SIZE; r++) {
            trackRow[index] = r;
            trackColumn[index] = SIZE - 1;
            index++;
        }

        // Bottom edge: (15,14) to (15,0)
        for (int c = SIZE - 2; c >= 0; c--) {
            trackRow[index] = SIZE - 1;
            trackColumn[index] = c;
            index++;
        }

        // Left edge: (14,0) to (1,0)
        for (int r = SIZE - 2; r >= 1; r--) {
            trackRow[index] = r;
            trackColumn[index] = 0;
            index++;
        }

        // Catch errors
        if (index != TRACK_LEN) {
            System.out.println("Index exceeded, error occurred.");
        }
    }

    // Step 1.2: Fill a printable grid with the track labels
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
        baseGrid[13][1] = "SA";
        baseGrid[13][2] = "FE";
        baseGrid[13][3] = "ZO";
        baseGrid[13][4] = "NE";
        baseGrid[13][5] = "!!";
        baseGrid[13][6] = "ST";

        baseGrid[9][13] = "ST";
        baseGrid[10][13] = "SA";
        baseGrid[11][13] = "FE";
        baseGrid[12][13] = "ZO";
        baseGrid[13][13] = "NE";
        baseGrid[14][13] = "!!";

    }

    public static String twoDigits(int n) {
        if (n < 10) return "0" + n;
        return "" + n;
    }

    public static void printGrid(String[][] grid) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                System.out.print("[" + grid[r][c] + "]");
            }
            System.out.println();
        }
    }

    public static void printPawns(int player) {
        System.out.println(PLAYER_COLORS[player] + "'s pawns:");
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            System.out.println(twoDigits(pawnPositions[player][pawn]) + ": " + PLAYER_COLORS[player]);
        }
    }

    public static void main(String[] args) {
        displayRules();

        initTrack();
        initBaseGrid();
        printGrid(baseGrid);
        System.out.println(trackColumn[30]);
        System.out.println(trackRow[30]);
        System.out.println();

    }

}

