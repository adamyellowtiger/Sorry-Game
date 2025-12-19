import java.util.Random;
import java.util.Scanner;

/**
 * Console-based SORRY! (simplified)
 * - Keeps the printable 16x16 grid as the board display.
 * - Implements the gameplay logic from SorryTest.java without ArrayList/Collections.
 *
 * Positions:
 *   -1  = Start
 *   0-59 = Main track
 *   60-64 = Home stretch (5 spaces)
 *   65 = Home
 *
 * @author Adam Fan
 */
public class SorryGood {

    // ===== Game constants =====
    static final int PLAYERS = 4;
    static final int PAWNS_PER_PLAYER = 4;

    static final int SIZE = 16;           // Grid size
    static final int BOARD_SIZE = 60;     // Main track size (perimeter of 16x16)
    static final int HOME_STRETCH_SIZE = 5; // Size of the grid leading up to home
    static final int HOME_POS = BOARD_SIZE + HOME_STRETCH_SIZE; // 65

    static final String[] PLAYER_COLORS = {"Red", "Blue", "Yellow", "Green"};
    static final char[] PLAYER_LETTER = {'R', 'B', 'Y', 'G'};

    // Starting positions for each player on the board
    static final int[] START_POSITIONS = {0, 15, 30, 45};

    // Home stretch entry positions for each player
    static final int[] HOME_ENTRY_POSITIONS = {2, 17, 32, 47};

    // State
    static int currentPlayer = 0;
    static int[][] pawnPositions = new int[PLAYERS][PAWNS_PER_PLAYER];
    static int[] savedSorryCards = new int[PLAYERS];

    // Set up Input
    static Scanner sc = new Scanner(System.in);
    static int nextInt() { return sc.nextInt(); }

    // Deck
    // Sorry! deck: 4 of each card 1-12 (no 6 or 9), plus 4 Sorry! cards (represented as 13)
    static final int[] CARD_TYPES = {1, 2, 3, 4, 5, 7, 8, 10, 11, 12, 13};
    static final int DECK_SIZE = 44; // 11 card types * 4
    static int[] deck = new int[DECK_SIZE];
    static int deckIndex = 0;
    static Random rng = new Random();

    // ===== Grid / track mapping =====
    static final int TRACK_LEN = BOARD_SIZE;
    static int[] trackRow = new int[TRACK_LEN];
    static int[] trackColumn = new int[TRACK_LEN];

    static String[][] baseGrid = new String[SIZE][SIZE];
    static String[][] upperGrid = new String[SIZE][SIZE];

    // Home-stretch drawing coordinates
    static int[][] homeRow = new int[PLAYERS][HOME_STRETCH_SIZE];
    static int[][] homeCol = new int[PLAYERS][HOME_STRETCH_SIZE];
    static int[] homeCellRow = new int[PLAYERS];
    static int[] homeCellCol = new int[PLAYERS];

    // ===== UI =====
    public static void displayRules() {
        System.out.println("\n=== SORRY! RULES (simplified) ===");
        System.out.println("Goal: Move all 4 pawns from Start to Home.");
        System.out.println("Draw cards to move. Land on opponents to send them back to Start!");
        System.out.println("Cards: 1,2 = Start or move | 3,5,8,12 = Forward | 4 = Backward");
        System.out.println("       7 = Split move | 10 = Forward 10 or Back 1 | 11 = Switch");
        System.out.println("       Sorry! = Bump opponent with pawn from Start OR save for later.");
        System.out.println("Players: Red, Blue, Yellow, Green.");
        System.out.println("=================================\n");
    }

    // Step 1.1: Build the 60-space track mapping (perimeter of a 16x16 square is 60, since 4*(16-1) = 60)
    public static void initTrack() {
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

        if (index != TRACK_LEN) {
            System.out.println("Track initialization mismatch: expected " + TRACK_LEN + ", and got " + index);
        }
    }

    // Step 1.2: Fill a printable grid with the track labels
    public static void initBaseGrid() {
        // Fill everything with empty spaces to ensure alignment (12 chars per cell)
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                baseGrid[r][c] = "            ";
            }
        }

        // Fill the perimeter with indices 0...59
        for (int i = 0; i < TRACK_LEN; i++) {
            int r = trackRow[i];
            int c = trackColumn[i];
            baseGrid[r][c] = twoDigits(i) + "          ";
        }

        // Your interior "SAFE ZONE!! ST" decorations
        baseGrid[13][1] = "SA          ";
        baseGrid[13][2] = "FE          ";
        baseGrid[13][3] = "ZO          ";
        baseGrid[13][4] = "NE          ";
        baseGrid[13][5] = "!!          ";
        baseGrid[13][6] = "ST          ";

        baseGrid[9][13]  = "ST          ";
        baseGrid[10][13] = "SA          ";
        baseGrid[11][13] = "FE          ";
        baseGrid[12][13] = "ZO          ";
        baseGrid[13][13] = "NE          ";
        baseGrid[14][13] = "!!          ";

        baseGrid[2][9]  = "ST          ";
        baseGrid[2][10] = "SA          ";
        baseGrid[2][11] = "FE          ";
        baseGrid[2][12] = "ZO          ";
        baseGrid[2][13] = "NE          ";
        baseGrid[2][14] = "!!          ";

        baseGrid[1][2] = "!!          ";
        baseGrid[2][2] = "SA          ";
        baseGrid[3][2] = "FE          ";
        baseGrid[4][2] = "ZO          ";
        baseGrid[5][2] = "NE          ";
        baseGrid[6][2] = "ST          ";

        initHomeStretchCoords();
    }

    // Choose some fixed squares inside my grid to represent each player's home stretch and home cell
    static void initHomeStretchCoords() {
        // Player 0 Red: row 13 columns 1..5, home at column 6
        for (int i = 0; i < HOME_STRETCH_SIZE; i++) {
            homeRow[0][i] = 13;
            homeCol[0][i] = 1 + i;
        }
        homeCellRow[0] = 13;
        homeCellCol[0] = 6;

        // Player 1 Blue: row 2 col 10..14, home at col 9
        for (int i = 0; i < HOME_STRETCH_SIZE; i++) {
            homeRow[1][i] = 2;
            homeCol[1][i] = 10 + i;
        }
        homeCellRow[1] = 2;
        homeCellCol[1] = 9;

        // Player 2 Yellow: col 13 row 10..14, home at row 9
        for (int i = 0; i < HOME_STRETCH_SIZE; i++) {
            homeRow[2][i] = 10 + i;
            homeCol[2][i] = 13;
        }
        homeCellRow[2] = 9;
        homeCellCol[2] = 13;

        // Player 3 Green: col 2 row 2..6, home at row 1
        for (int i = 0; i < HOME_STRETCH_SIZE; i++) {
            homeRow[3][i] = 2 + i;
            homeCol[3][i] = 2;
        }
        homeCellRow[3] = 1;
        homeCellCol[3] = 2;
    }

    // ===== Formatting helpers =====
    public static String twoDigits(int n) {
        if (n < 0) return "--";
        if (n < 10) return "0" + n;
        return "" + n;
    }

    static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < width) sb.append(' ');
        return sb.toString();
    }

    // Create a 12-char cell: XX + space + token + spaces...
    static String makeCell(String prefix2, String token) {
        String base = prefix2 + " " + token;
        return padRight(base, 12);
    }

    // If you want to keep the original 2-letter label (SA/FE/etc) and add a pawn token
    static String label2FromCell(String cell) {
        if (cell == null || cell.length() < 2) return "??";
        return cell.substring(0, 2);
    }

    // ===== Grid printing =====
    public static void printGrid(String[][] grid) {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                System.out.print("[" + grid[r][c] + "]");
            }
            System.out.println();
        }
    }

    // Build upperGrid from baseGrid + overlay pawns
    static void buildDisplayGrid() {
        // Copy base -> upper
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                upperGrid[r][c] = baseGrid[r][c];
            }
        }

        // Overlay pawns
        for (int p = 0; p < PLAYERS; p++) {
            for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
                int pos = pawnPositions[p][pawn];
                if (pos >= 0 && pos < BOARD_SIZE) {
                    int rr = trackRow[pos];
                    int cc = trackColumn[pos];
                    String token = "" + PLAYER_LETTER[p] + (pawn + 1);
                    upperGrid[rr][cc] = makeCell(twoDigits(pos), token);
                } else if (pos >= BOARD_SIZE && pos < HOME_POS) {
                    int hs = pos - BOARD_SIZE; // 0..4
                    int rr = homeRow[p][hs];
                    int cc = homeCol[p][hs];
                    String token = "" + PLAYER_LETTER[p] + (pawn + 1);
                    String label2 = label2FromCell(baseGrid[rr][cc]);
                    upperGrid[rr][cc] = makeCell(label2, token);
                } else if (pos == HOME_POS) {
                    int rr = homeCellRow[p];
                    int cc = homeCellCol[p];
                    String token = "" + PLAYER_LETTER[p] + (pawn + 1);
                    upperGrid[rr][cc] = makeCell("HM", token);
                }
            }
        }
    }

    // ===== Board status =====
    static void displayBoard() {
        buildDisplayGrid();
        printGrid(upperGrid);

        System.out.println("\n=== PAWN STATUS ===");
        for (int player = 0; player < PLAYERS; player++) {
            System.out.print(PLAYER_COLORS[player] + ": ");
            for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
                System.out.print("[" + getPositionDescription(player, pawnPositions[player][pawn]) + "] ");
            }
            System.out.println();
        }
        System.out.println("===================\n");
    }

    static String getPositionDescription(int player, int pos) {
        if (pos == -1) return "Start";
        if (pos == HOME_POS) return "HOME";
        if (pos >= BOARD_SIZE && pos < HOME_POS) return "HomeStretch " + (pos - BOARD_SIZE);
        return "Track " + pos;
    }

    static void nextPlayer() { currentPlayer = (currentPlayer + 1) % PLAYERS; }

    // ===== Game setup / deck =====
    static void initializeGame() {
        for (int i = 0; i < PLAYERS; i++) {
            for (int j = 0; j < PAWNS_PER_PLAYER; j++) {
                pawnPositions[i][j] = -1;
            }
            savedSorryCards[i] = 0;
        }
        resetDeck();
    }

    static void resetDeck() {
        int idx = 0;
        for (int t = 0; t < CARD_TYPES.length; t++) {
            for (int k = 0; k < 4; k++) {
                deck[idx++] = CARD_TYPES[t];
            }
        }
        // Fisher-Yates shuffle
        for (int i = deck.length - 1; i > 0; i--) {
            int j = rng.nextInt(i + 1);
            int tmp = deck[i];
            deck[i] = deck[j];
            deck[j] = tmp;
        }
        deckIndex = 0;
    }

    static int drawCard() {
        if (deckIndex >= deck.length) resetDeck();
        return deck[deckIndex++];
    }

    static String getCardName(int card) {
        return card == 13 ? "Sorry!" : String.valueOf(card);
    }

    static void printCardAction(int card) {
        switch (card) {
            case 1:  System.out.println("  -> Move a pawn from Start OR move forward 1 space."); break;
            case 2:  System.out.println("  -> Move a pawn from Start OR move forward 2 spaces. Draw again!"); break;
            case 3:  System.out.println("  -> Move forward 3 spaces."); break;
            case 4:  System.out.println("  -> Move backward 4 spaces."); break;
            case 5:  System.out.println("  -> Move forward 5 spaces."); break;
            case 7:  System.out.println("  -> Move forward 7 spaces OR split between 2 pawns."); break;
            case 8:  System.out.println("  -> Move forward 8 spaces."); break;
            case 10: System.out.println("  -> Move forward 10 spaces OR move backward 1 space."); break;
            case 11: System.out.println("  -> Move forward 11 spaces OR switch with an opponent."); break;
            case 12: System.out.println("  -> Move forward 12 spaces."); break;
            case 13: System.out.println("  -> Sorry! Bump an opponent now OR save for later."); break;
        }
    }

    // ===== Turn / card logic =====
    static boolean playCard(int player, int card) {
        switch (card) {
            case 1:
                playCard1(player);
                break;
            case 2:
                playCard2(player);
                return true; // draw again
            case 3:
                moveForward(player, 3);
                break;
            case 4:
                moveBackward(player, 4);
                break;
            case 5:
                moveForward(player, 5);
                break;
            case 7:
                playCard7(player);
                break;
            case 8:
                moveForward(player, 8);
                break;
            case 10:
                playCard10(player);
                break;
            case 11:
                playCard11(player);
                break;
            case 12:
                moveForward(player, 12);
                break;
            case 13:
                playCard13(player);
                break;
        }
        return false;
    }

    static void playCard1(int player) {
        System.out.println("Choose action: (1) Move pawn from Start, (2) Move forward 1 space");
        int choice = nextInt();
        if (choice == 1) moveFromStart(player);
        else moveForward(player, 1);
    }

    static void playCard2(int player) {
        System.out.println("Choose action: (1) Move pawn from Start, (2) Move forward 2 spaces");
        int choice = nextInt();
        if (choice == 1) moveFromStart(player);
        else moveForward(player, 2);
    }

    static void playCard7(int player) {
        System.out.println("Choose action: (1) Move one pawn 7 spaces, (2) Split between two pawns");
        int choice = nextInt();
        if (choice == 1) {
            moveForward(player, 7);
        } else {
            System.out.println("Enter spaces for first pawn (1-6):");
            int first = nextInt();
            if (first < 1) first = 1;
            if (first > 6) first = 6;
            int second = 7 - first;
            System.out.println("First pawn moves " + first + ", second pawn moves " + second);

            System.out.println("Select first pawn to move:");
            int pawn1 = selectPawn(player, true);
            if (pawn1 != -1) movePawn(player, pawn1, first, true);

            System.out.println("Select second pawn to move:");
            int pawn2 = selectPawn(player, true);
            if (pawn2 != -1) movePawn(player, pawn2, second, true);
        }
    }

    static void playCard10(int player) {
        System.out.println("Choose action: (1) Move forward 10 spaces, (2) Move backward 1 space");
        int choice = nextInt();
        if (choice == 1) moveForward(player, 10);
        else moveBackward(player, 1);
    }

    static void playCard11(int player) {
        System.out.println("Choose action: (1) Move forward 11 spaces, (2) Switch with opponent");
        int choice = nextInt();
        if (choice == 1) moveForward(player, 11);
        else switchWithOpponent(player);
    }

    static void playCard13(int player) {
        System.out.println("Choose action: (1) Use Sorry! now, (2) Save for later");
        int choice = nextInt();
        if (choice == 1) playSorryCard(player);
        else {
            savedSorryCards[player]++;
            System.out.println("Sorry! card saved. You now have " + savedSorryCards[player] + " saved.");
        }
    }

    // ===== Moves =====
    static void moveFromStart(int player) {
        // find a pawn in start
        int startPawn = -1;
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] == -1) { startPawn = pawn; break; }
        }
        if (startPawn == -1) {
            System.out.println("No pawns in Start!");
            return;
        }

        int startPos = START_POSITIONS[player];

        // blocked by own pawn?
        if (isOwnPawnAt(player, startPos)) {
            System.out.println("Cannot move from Start - blocked by your own pawn!");
            return;
        }

        pawnPositions[player][startPawn] = startPos;
        System.out.println("Pawn " + (startPawn + 1) + " moved from Start to position " + startPos);
        checkBump(player, startPawn);
        checkSlide(player, startPawn);
    }

    static void moveForward(int player, int spaces) {
        int pawn = selectPawn(player, true);
        if (pawn != -1) movePawn(player, pawn, spaces, true);
    }

    static void moveBackward(int player, int spaces) {
        int pawn = selectPawn(player, true);
        if (pawn != -1) movePawn(player, pawn, -spaces, true);
    }

    static int selectPawn(int player, boolean onBoardOnly) {
        boolean[] canPick = new boolean[PAWNS_PER_PLAYER];
        int count = 0;

        System.out.println("Your pawns:");
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            int pos = pawnPositions[player][pawn];
            System.out.println((pawn + 1) + ": " + getPositionDescription(player, pos));
            if (!onBoardOnly || (pos >= 0 && pos < HOME_POS)) {
                canPick[pawn] = true;
                count++;
            }
        }

        if (count == 0) {
            System.out.println("No pawns available to move!");
            return -1;
        }

        System.out.println("Select pawn (1-4):");
        int choice = nextInt() - 1;
        if (choice >= 0 && choice < PAWNS_PER_PLAYER && canPick[choice]) return choice;

        System.out.println("Invalid selection!");
        return -1;
    }

    static void movePawn(int player, int pawn, int spaces, boolean forward) {
        int currentPos = pawnPositions[player][pawn];

        if (currentPos == -1) {
            System.out.println("This pawn is at Start and cannot move that way!");
            return;
        }
        if (currentPos == HOME_POS) {
            System.out.println("This pawn is already HOME!");
            return;
        }

        // Pawn is in home stretch
        if (currentPos >= BOARD_SIZE && currentPos < HOME_POS) {
            int homeStretchPos = currentPos - BOARD_SIZE;
            int newHomeStretchPos = homeStretchPos + spaces;

            if (newHomeStretchPos >= HOME_STRETCH_SIZE) {
                pawnPositions[player][pawn] = HOME_POS;
                System.out.println("Pawn " + (pawn + 1) + " reached HOME!");
            } else if (newHomeStretchPos >= 0) {
                pawnPositions[player][pawn] = BOARD_SIZE + newHomeStretchPos;
                System.out.println("Pawn " + (pawn + 1) + " moved in home stretch to position " + newHomeStretchPos);
            } else {
                System.out.println("Cannot move backward past start of home stretch.");
            }
            return;
        }

        // On main board
        int newPos = (currentPos + spaces + BOARD_SIZE) % BOARD_SIZE;

        // Enter home stretch if passing home entry (forward moves only)
        int homeEntry = HOME_ENTRY_POSITIONS[player];
        if (forward && spaces > 0) {
            int distanceToHome = (homeEntry - currentPos + BOARD_SIZE) % BOARD_SIZE;
            if (distanceToHome > 0 && distanceToHome <= spaces) {
                int remaining = spaces - distanceToHome;
                if (remaining <= HOME_STRETCH_SIZE) {
                    pawnPositions[player][pawn] = BOARD_SIZE + remaining - 1; // 0..4
                    System.out.println("Pawn " + (pawn + 1) + " entered home stretch at position " + (remaining - 1));
                    if (remaining == HOME_STRETCH_SIZE) {
                        pawnPositions[player][pawn] = HOME_POS;
                        System.out.println("Pawn " + (pawn + 1) + " reached HOME!");
                    }
                    return;
                }
            }
        }

        // Blocked by own pawn
        if (isOwnPawnAt(player, newPos)) {
            System.out.println("Cannot move - blocked by your own pawn!");
            return;
        }

        pawnPositions[player][pawn] = newPos;
        System.out.println("Pawn " + (pawn + 1) + " moved to position " + newPos);
        checkBump(player, pawn);
        checkSlide(player, pawn);
    }

    // ===== Interactions =====
    static boolean isOwnPawnAt(int player, int pos) {
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] == pos) return true;
        }
        return false;
    }

    static void checkBump(int player, int pawn) {
        int pos = pawnPositions[player][pawn];
        if (pos < 0 || pos >= BOARD_SIZE) return; // only bump on main track

        for (int p = 0; p < PLAYERS; p++) {
            if (p == player) continue;
            for (int otherPawn = 0; otherPawn < PAWNS_PER_PLAYER; otherPawn++) {
                if (pawnPositions[p][otherPawn] == pos) {
                    pawnPositions[p][otherPawn] = -1;
                    System.out.println("BUMP! " + PLAYER_COLORS[p] + "'s pawn sent back to Start!");
                }
            }
        }
    }

    static void checkSlide(int player, int pawn) {
        int pos = pawnPositions[player][pawn];
        if (pos < 0 || pos >= BOARD_SIZE) return;

        // One slide per color, length 4 (matches SorryTest logic)
        int[] slideStart = {1, 16, 31, 46};
        int slideLen = 4;

        for (int colorOwner = 0; colorOwner < PLAYERS; colorOwner++) {
            if (colorOwner == player) continue; // can't slide on your own color
            if (pos == slideStart[colorOwner]) {
                int endPos = (pos + slideLen) % BOARD_SIZE;
                pawnPositions[player][pawn] = endPos;
                System.out.println("SLIDE! Moved to position " + endPos);

                // Bump any pawns on the slide path
                for (int step = 1; step <= slideLen; step++) {
                    int slidePos = (pos + step) % BOARD_SIZE;
                    for (int op = 0; op < PLAYERS; op++) {
                        for (int opPawn = 0; opPawn < PAWNS_PER_PLAYER; opPawn++) {
                            if (pawnPositions[op][opPawn] == slidePos) {
                                pawnPositions[op][opPawn] = -1;
                                System.out.println(PLAYER_COLORS[op] + "'s pawn bumped by slide!");
                            }
                        }
                    }
                }
                return;
            }
        }
    }

    static void switchWithOpponent(int player) {
        // gather targets
        int[] tPlayer = new int[12];
        int[] tPawn = new int[12];
        int[] tPos = new int[12];
        int tCount = 0;

        for (int p = 0; p < PLAYERS; p++) {
            if (p == player) continue;
            for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
                int pos = pawnPositions[p][pawn];
                if (pos >= 0 && pos < BOARD_SIZE) {
                    tPlayer[tCount] = p;
                    tPawn[tCount] = pawn;
                    tPos[tCount] = pos;
                    tCount++;
                }
            }
        }

        if (tCount == 0) {
            System.out.println("No opponent pawns available to switch with!");
            return;
        }

        int myPawn = selectPawn(player, true);
        if (myPawn == -1) return;

        int myPos = pawnPositions[player][myPawn];
        if (myPos < 0 || myPos >= BOARD_SIZE) {
            System.out.println("You need a pawn on the main board to switch!");
            return;
        }

        System.out.println("Available targets to switch:");
        for (int i = 0; i < tCount; i++) {
            System.out.println((i + 1) + ": " + PLAYER_COLORS[tPlayer[i]] + " pawn at position " + tPos[i]);
        }
        System.out.println("Select target (1-" + tCount + "):");
        int choice = nextInt() - 1;
        if (choice < 0 || choice >= tCount) {
            System.out.println("Invalid target!");
            return;
        }

        int otherPlayer = tPlayer[choice];
        int otherPawn = tPawn[choice];
        int theirPos = tPos[choice];

        pawnPositions[player][myPawn] = theirPos;
        pawnPositions[otherPlayer][otherPawn] = myPos;
        System.out.println("Switched positions! You are now at " + theirPos);
    }

    static void playSorryCard(int player) {
        // targets on main board
        int[] tPlayer = new int[12];
        int[] tPawn = new int[12];
        int[] tPos = new int[12];
        int tCount = 0;

        for (int p = 0; p < PLAYERS; p++) {
            if (p == player) continue;
            for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
                int pos = pawnPositions[p][pawn];
                if (pos >= 0 && pos < BOARD_SIZE) {
                    tPlayer[tCount] = p;
                    tPawn[tCount] = pawn;
                    tPos[tCount] = pos;
                    tCount++;
                }
            }
        }

        if (tCount == 0) {
            System.out.println("No opponent pawns available to bump!");
            return;
        }

        // must have a pawn in start
        int startPawn = -1;
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] == -1) { startPawn = pawn; break; }
        }
        if (startPawn == -1) {
            System.out.println("You have no pawns in Start to use Sorry!");
            return;
        }

        System.out.println("Available targets:");
        for (int i = 0; i < tCount; i++) {
            System.out.println((i + 1) + ": " + PLAYER_COLORS[tPlayer[i]] + " pawn at position " + tPos[i]);
        }
        System.out.println("Select target (1-" + tCount + "):");
        int choice = nextInt() - 1;
        if (choice < 0 || choice >= tCount) {
            System.out.println("Invalid target!");
            return;
        }

        int targetPlayer = tPlayer[choice];
        int targetPawn = tPawn[choice];
        int targetPos = tPos[choice];

        pawnPositions[targetPlayer][targetPawn] = -1;
        System.out.println(PLAYER_COLORS[targetPlayer] + "'s pawn sent back to Start!");

        pawnPositions[player][startPawn] = targetPos;
        System.out.println(PLAYER_COLORS[player] + "'s pawn moved to position " + targetPos);

        checkSlide(player, startPawn);
    }

    // ===== Win condition =====
    static boolean checkWin(int player) {
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] != HOME_POS) return false;
        }
        return true;
    }

    // ===== Main =====
    public static void main(String[] args) {
        displayRules();

        initTrack();
        initBaseGrid();
        initializeGame();

        boolean gameOver = false;

        while (!gameOver) {
            System.out.println("\n--- " + PLAYER_COLORS[currentPlayer] + "'s turn ---");
            displayBoard();

            // Saved Sorry? card option
            if (savedSorryCards[currentPlayer] > 0) {
                System.out.println("You have " + savedSorryCards[currentPlayer] + " saved Sorry! card(s).");
                System.out.println("Do you want to use a Sorry! card? (1 = Yes, 0 = No)");
                int useSorry = nextInt();
                if (useSorry == 1) {
                    playSorryCard(currentPlayer);
                    savedSorryCards[currentPlayer]--;
                    if (checkWin(currentPlayer)) {
                        gameOver = true;
                        break;
                    }
                    nextPlayer();
                    continue;
                }
            }

            int card = drawCard();
            System.out.println(PLAYER_COLORS[currentPlayer] + " drew a " + getCardName(card));
            printCardAction(card);

            boolean playAgain = playCard(currentPlayer, card);

            if (checkWin(currentPlayer)) {
                gameOver = true;
                break;
            }

            if (!playAgain) nextPlayer();
            else System.out.println(PLAYER_COLORS[currentPlayer] + " draws again!");
        }

        System.out.println("\n" + PLAYER_COLORS[currentPlayer] + " WINS!");
    }
}
