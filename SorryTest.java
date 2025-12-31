import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * @author Adam Fan
 * @date 2025-12-12
 */

/**
 * The SorryTest class drives a text-based version of the SORRY! board game.
 * It bundles the card drawing, pawn movement, and win checking so four people
 * can take turns and race their pawns home.
 */
public class SorryTest {
    /*
     * Input helpers keep the console reads tidy so we can focus on game rules
     * instead of parsing.
     */
    // Input handling
    static Scanner scanner = new Scanner(System.in);

    /**
     * Reads the next token from standard input, grabbing a new token from the
     * scanner if needed.
     */
    static String next() throws Exception {
        while (!scanner.hasNext()) {
            scanner.nextLine();
        }
        return scanner.next();
    }

    /**
     * Parses the next token as an integer so card choices can be read safely.
     */
    static int nextInt() throws Exception {
        while (!scanner.hasNextInt()) {
            scanner.next();
        }
        return scanner.nextInt();
    }

    /* Game constants that set up the board and player details. */
    static final int PLAYERS = 4;
    static final int BOARD_SIZE = 60;
    static final int PAWNS_PER_PLAYER = 4;
    static final int HOME_STRETCH_SIZE = 5;
    static final String[] PLAYER_COLORS = {"Red", "Blue", "Yellow", "Green"};

    /* Card deck (no 6 or 9 in Sorry!) */
    static List<Integer> deck = new ArrayList<>();
    static Random random = new Random();

    /*
     * Player pawns: [player][pawn] = position
     * -1 means Start, 0-59 covers the main ring, 60-64 is the safety zone,
     * and 65 marks a pawn that made it all the way Home.
     */
    static int[][] pawnPositions = new int[PLAYERS][PAWNS_PER_PLAYER];

    /* Starting positions for each player on the board. */
    static final int[] START_POSITIONS = {0, 15, 30, 45};

    /* Home stretch entry positions for each player. */
    static final int[] HOME_ENTRY_POSITIONS = {2, 17, 32, 47};

    /* Current player index. */
    static int currentPlayer = 0;

    /* Saved Sorry! cards for each player. */
    static int[] savedSorryCards = new int[PLAYERS];

    /**
     * Entry point: shows the welcome text, sets up the board, and loops turn by
     * turn until someone gets every pawn home.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== WELCOME TO SORRY! ===");
        System.out.println("This game requires four players. Please ensure that you have four players to play.\n");

        initializeGame();
        printRules();

        boolean gameOver = false;
        while (!gameOver) {
            System.out.println("\n--- " + PLAYER_COLORS[currentPlayer] + "'s Turn ---");
            displayBoard();

            // Check if player has a saved Sorry! card
            if (savedSorryCards[currentPlayer] > 0) {
                System.out.println("You have " + savedSorryCards[currentPlayer] + " saved Sorry! card(s).");
                System.out.println("Do you want to use a Sorry! card? (1 = Yes, 0 = No)");
                int useSorry = nextInt();
                if (useSorry == 1) {
                    playSorryCard(currentPlayer);
                    savedSorryCards[currentPlayer]--;
                    if (checkWin(currentPlayer)) {
                        gameOver = true;
                        continue;
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
                continue;
            }

            if (!playAgain) {
                nextPlayer();
            } else {
                System.out.println(PLAYER_COLORS[currentPlayer] + " gets to draw again!");
            }
        }

        System.out.println("\n🎉 " + PLAYER_COLORS[currentPlayer] + " WINS! 🎉");
    }

    /**
     * Sets pawns to Start, clears saved cards, and shuffles a fresh deck so the
     * match begins with a clean slate.
     */
    static void initializeGame() {
        // Initialize all pawns at start (-1)
        for (int i = 0; i < PLAYERS; i++) {
            for (int j = 0; j < PAWNS_PER_PLAYER; j++) {
                pawnPositions[i][j] = -1;
            }
            savedSorryCards[i] = 0;
        }

        // Initialize and shuffle deck
        resetDeck();
    }

    /**
     * Fills the deck with four of each card type, then shuffles to randomize the
     * draw order.
     */
    static void resetDeck() {
        deck.clear();
        // Sorry! deck: 4 of each card 1-12 (no 6 or 9), plus 4 Sorry! cards (represented as 13)
        int[] cardTypes = {1, 2, 3, 4, 5, 7, 8, 10, 11, 12, 13};
        for (int card : cardTypes) {
            for (int i = 0; i < 4; i++) {
                deck.add(card);
            }
        }
        shuffleDeck();
    }

    /**
     * Grabs the top card, reshuffling if the pile ran out.
     */
    static int drawCard() {
        if (deck.isEmpty()) {
            resetDeck();
        }
        return deck.remove(deck.size() - 1);
    }

    /**
     * Simple manual shuffle using Random so it fits what we learned.
     */
    static void shuffleDeck() {
        for (int i = deck.size() - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, temp);
        }
    }

    /**
     * Turns the numeric card ID into a readable label.
     */
    static String getCardName(int card) {
        return card == 13 ? "Sorry!" : String.valueOf(card);
    }

    /**
     * Prints the quick rules for the drawn card so players know their options.
     */
    static void printCardAction(int card) {
        switch (card) {
            case 1:
                System.out.println("  → Move a pawn from Start OR move forward 1 space.");
                break;
            case 2:
                System.out.println("  → Move a pawn from Start OR move forward 2 spaces. Draw again!");
                break;
            case 3:
                System.out.println("  → Move forward 3 spaces.");
                break;
            case 4:
                System.out.println("  → Move backward 4 spaces.");
                break;
            case 5:
                System.out.println("  → Move forward 5 spaces.");
                break;
            case 7:
                System.out.println("  → Move forward 7 spaces OR split between 2 pawns.");
                break;
            case 8:
                System.out.println("  → Move forward 8 spaces.");
                break;
            case 10:
                System.out.println("  → Move forward 10 spaces OR move backward 1 space.");
                break;
            case 11:
                System.out.println("  → Move forward 11 spaces OR switch with an opponent.");
                break;
            case 12:
                System.out.println("  → Move forward 12 spaces.");
                break;
            case 13:
                System.out.println("  → Sorry! Bump an opponent to Start OR save for later.");
                break;
        }
    }

    /**
     * Routes the drawn card to the correct rule handler and reports whether the
     * player should take another turn (card 2).
     */
    static boolean playCard(int player, int card) throws Exception {
        switch (card) {
            case 1:
                playCard1(player);
                break;
            case 2:
                playCard2(player);
                return true; // Draw again
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

    /**
     * Card 1 lets a player leave Start or inch forward one space.
     */
    static void playCard1(int player) throws Exception {
        System.out.println("Choose action: (1) Move pawn from Start, (2) Move forward 1 space");
        int choice = nextInt();
        if (choice == 1) {
            moveFromStart(player);
        } else {
            moveForward(player, 1);
        }
    }

    /**
     * Card 2 mirrors card 1 but awards another draw after the move.
     */
    static void playCard2(int player) throws Exception {
        System.out.println("Choose action: (1) Move pawn from Start, (2) Move forward 2 spaces");
        int choice = nextInt();
        if (choice == 1) {
            moveFromStart(player);
        } else {
            moveForward(player, 2);
        }
    }

    /**
     * Card 7 can be spent on one big push or split into two smaller hops.
     */
    static void playCard7(int player) throws Exception {
        System.out.println("Choose action: (1) Move one pawn 7 spaces, (2) Split between two pawns");
        int choice = nextInt();
        if (choice == 1) {
            moveForward(player, 7);
        } else {
            System.out.println("Enter spaces for first pawn (1-6):");
            int first = nextInt();
            int second = 7 - first;
            System.out.println("First pawn moves " + first + ", second pawn moves " + second);

            System.out.println("Select first pawn to move:");
            int pawn1 = selectPawn(player, true);
            if (pawn1 != -1) {
                movePawn(player, pawn1, first, true);
            }

            System.out.println("Select second pawn to move:");
            int pawn2 = selectPawn(player, true);
            if (pawn2 != -1) {
                movePawn(player, pawn2, second, true);
            }
        }
    }

    /**
     * Card 10 offers a burst forward or a tiny retreat.
     */
    static void playCard10(int player) throws Exception {
        System.out.println("Choose action: (1) Move forward 10 spaces, (2) Move backward 1 space");
        int choice = nextInt();
        if (choice == 1) {
            moveForward(player, 10);
        } else {
            moveBackward(player, 1);
        }
    }

    /**
     * Card 11 either pushes ahead or swaps spots with an opposing pawn.
     */
    static void playCard11(int player) throws Exception {
        System.out.println("Choose action: (1) Move forward 11 spaces, (2) Switch with opponent");
        int choice = nextInt();
        if (choice == 1) {
            moveForward(player, 11);
        } else {
            switchWithOpponent(player);
        }
    }

    /**
     * Card 13 is the iconic Sorry! bump. Players can cash it in now or stash it.
     */
    static void playCard13(int player) throws Exception {
        System.out.println("Choose action: (1) Use Sorry! now, (2) Save for later");
        int choice = nextInt();
        if (choice == 1) {
            playSorryCard(player);
        } else {
            savedSorryCards[player]++;
            System.out.println("Sorry! card saved. You now have " + savedSorryCards[player] + " saved.");
        }
    }

    /**
     * Uses a Sorry! card to boot an opponent off the main track and replace them
     * with one of the player's start pawns.
     */
    static void playSorryCard(int player) throws Exception {
        // Find opponent pawns on the board (not in start, home stretch, or home)
        List<int[]> targets = new ArrayList<>();
        for (int p = 0; p < PLAYERS; p++) {
            if (p == player) continue;
            for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
                int pos = pawnPositions[p][pawn];
                if (pos >= 0 && pos < BOARD_SIZE) {
                    targets.add(new int[]{p, pawn, pos});
                }
            }
        }

        if (targets.isEmpty()) {
            System.out.println("No opponent pawns available to bump!");
            return;
        }

        // Must have a pawn in start
        int startPawn = -1;
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] == -1) {
                startPawn = pawn;
                break;
            }
        }

        if (startPawn == -1) {
            System.out.println("You have no pawns in Start to move!");
            return;
        }

        System.out.println("Available targets:");
        for (int i = 0; i < targets.size(); i++) {
            int[] target = targets.get(i);
            System.out.println((i + 1) + ": " + PLAYER_COLORS[target[0]] + " pawn at position " + target[2]);
        }
        System.out.println("Select target (1-" + targets.size() + "):");
        int choice = nextInt() - 1;

        if (choice >= 0 && choice < targets.size()) {
            int[] target = targets.get(choice);
            int targetPlayer = target[0];
            int targetPawn = target[1];
            int targetPos = target[2];

            // Send opponent pawn back to start
            pawnPositions[targetPlayer][targetPawn] = -1;
            System.out.println(PLAYER_COLORS[targetPlayer] + "'s pawn sent back to Start!");

            // Move your pawn to that position
            pawnPositions[player][startPawn] = targetPos;
            System.out.println(PLAYER_COLORS[player] + "'s pawn moved to position " + targetPos);
        }
    }

    /**
     * Moves the first available pawn out of Start onto its color's opening tile,
     * handling bumps if the landing spot is occupied.
     */
    static void moveFromStart(int player) {
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] == -1) {
                int startPos = START_POSITIONS[player];
                // Check if own pawn is blocking
                if (!isOwnPawnAt(player, startPos)) {
                    pawnPositions[player][pawn] = startPos;
                    System.out.println("Pawn " + (pawn + 1) + " moved from Start to position " + startPos);
                    checkBump(player, pawn);
                    return;
                }
            }
        }
        System.out.println("Cannot move from Start - no pawns available or blocked!");
    }

    /**
     * Lets the player pick a pawn and step it forward the requested count.
     */
    static void moveForward(int player, int spaces) throws Exception {
        int pawn = selectPawn(player, true);
        if (pawn != -1) {
            movePawn(player, pawn, spaces, true);
        }
    }

    /**
     * Lets the player pick a pawn and walk it backward for cards like 4 or the
     * reverse option on 10.
     */
    static void moveBackward(int player, int spaces) throws Exception {
        int pawn = selectPawn(player, true);
        if (pawn != -1) {
            movePawn(player, pawn, -spaces, true);
        }
    }

    /**
     * Displays each pawn's status and confirms the selection is allowed for the
     * current card.
     */
    static int selectPawn(int player, boolean onBoardOnly) throws Exception {
        List<Integer> available = new ArrayList<>();
        System.out.println("Your pawns:");
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            int pos = pawnPositions[player][pawn];
            String location = getPositionDescription(player, pos);
            System.out.println((pawn + 1) + ": " + location);
            if (!onBoardOnly || (pos >= 0 && pos < BOARD_SIZE + HOME_STRETCH_SIZE)) {
                available.add(pawn);
            }
        }

        if (available.isEmpty()) {
            System.out.println("No pawns available to move!");
            return -1;
        }

        System.out.println("Select pawn (1-4):");
        int choice = nextInt() - 1;
        if (choice >= 0 && choice < PAWNS_PER_PLAYER && available.contains(choice)) {
            return choice;
        }
        System.out.println("Invalid selection!");
        return -1;
    }

    /**
     * Handles all the movement math for a pawn, including entering the home
     * stretch, bumping, and sliding.
     */
    static void movePawn(int player, int pawn, int spaces, boolean forward) {
        int currentPos = pawnPositions[player][pawn];

        if (currentPos == -1) {
            System.out.println("This pawn is at Start and cannot move!");
            return;
        }

        if (currentPos >= BOARD_SIZE) {
            // Pawn is in home stretch
            int homeStretchPos = currentPos - BOARD_SIZE;
            int newHomeStretchPos = homeStretchPos + spaces;
            if (newHomeStretchPos >= HOME_STRETCH_SIZE) {
                pawnPositions[player][pawn] = BOARD_SIZE + HOME_STRETCH_SIZE; // Home!
                System.out.println("Pawn " + (pawn + 1) + " reached HOME!");
            } else if (newHomeStretchPos >= 0) {
                pawnPositions[player][pawn] = BOARD_SIZE + newHomeStretchPos;
                System.out.println("Pawn " + (pawn + 1) + " moved in home stretch to position " + newHomeStretchPos);
            }
            return;
        }

        int newPos = (currentPos + spaces + BOARD_SIZE) % BOARD_SIZE;

        // Check if passing home entry
        int homeEntry = HOME_ENTRY_POSITIONS[player];
        if (forward && spaces > 0) {
            int distanceToHome = (homeEntry - currentPos + BOARD_SIZE) % BOARD_SIZE;
            if (distanceToHome > 0 && distanceToHome <= spaces) {
                // Enter home stretch
                int remaining = spaces - distanceToHome;
                if (remaining <= HOME_STRETCH_SIZE) {
                    pawnPositions[player][pawn] = BOARD_SIZE + remaining - 1;
                    System.out.println("Pawn " + (pawn + 1) + " entered home stretch at position " + (remaining - 1));
                    if (remaining == HOME_STRETCH_SIZE) {
                        pawnPositions[player][pawn] = BOARD_SIZE + HOME_STRETCH_SIZE;
                        System.out.println("Pawn " + (pawn + 1) + " reached HOME!");
                    }
                    return;
                }
            }
        }

        // Check if blocked by own pawn
        if (isOwnPawnAt(player, newPos)) {
            System.out.println("Cannot move - blocked by your own pawn!");
            return;
        }

        pawnPositions[player][pawn] = newPos;
        System.out.println("Pawn " + (pawn + 1) + " moved to position " + newPos);
        checkBump(player, pawn);
        checkSlide(player, pawn);
    }

    /**
     * Swaps a chosen pawn with an opponent's pawn that is sitting on the main
     * loop of the board.
     */
    static void switchWithOpponent(int player) throws Exception {
        List<int[]> targets = new ArrayList<>();
        for (int p = 0; p < PLAYERS; p++) {
            if (p == player) continue;
            for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
                int pos = pawnPositions[p][pawn];
                if (pos >= 0 && pos < BOARD_SIZE) {
                    targets.add(new int[]{p, pawn, pos});
                }
            }
        }

        if (targets.isEmpty()) {
            System.out.println("No opponent pawns available to switch with!");
            return;
        }

        int myPawn = selectPawn(player, true);
        if (myPawn == -1 || pawnPositions[player][myPawn] < 0 || pawnPositions[player][myPawn] >= BOARD_SIZE) {
            System.out.println("You need a pawn on the main board to switch!");
            return;
        }

        System.out.println("Available targets to switch:");
        for (int i = 0; i < targets.size(); i++) {
            int[] target = targets.get(i);
            System.out.println((i + 1) + ": " + PLAYER_COLORS[target[0]] + " pawn at position " + target[2]);
        }
        System.out.println("Select target (1-" + targets.size() + "):");
        int choice = nextInt() - 1;

        if (choice >= 0 && choice < targets.size()) {
            int[] target = targets.get(choice);
            int myPos = pawnPositions[player][myPawn];
            int theirPos = target[2];

            pawnPositions[player][myPawn] = theirPos;
            pawnPositions[target[0]][target[1]] = myPos;
            System.out.println("Switched positions! You are now at " + theirPos);
        }
    }

    /**
     * Checks if a position already has one of the player's own pawns.
     */
    static boolean isOwnPawnAt(int player, int pos) {
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] == pos) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sends any opponent pawn occupying the same tile back to Start.
     */
    static void checkBump(int player, int pawn) {
        int pos = pawnPositions[player][pawn];
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

    /**
     * Applies the slide rules: landing on another color's slide start moves the
     * pawn forward and bumps any pieces on the slide path.
     */
    static void checkSlide(int player, int pawn) {
        int pos = pawnPositions[player][pawn];
        // Slide positions (start of slides for opponents)
        int[][] slides = {
            {1, 4},   // Red's slides
            {16, 4},  // Blue's slides
            {31, 4},  // Yellow's slides
            {46, 4}   // Green's slides
        };

        for (int p = 0; p < PLAYERS; p++) {
            if (p == player) continue; // Can't slide on your own color
            for (int[] slide : new int[][]{{slides[p][0], slides[p][1]}}) {
                if (pos == slide[0]) {
                    int endPos = (pos + slide[1]) % BOARD_SIZE;
                    pawnPositions[player][pawn] = endPos;
                    System.out.println("SLIDE! Moved to position " + endPos);

                    // Bump any pawns on the slide
                    for (int slidePos = pos + 1; slidePos <= pos + slide[1]; slidePos++) {
                        int actualPos = slidePos % BOARD_SIZE;
                        for (int op = 0; op < PLAYERS; op++) {
                            for (int opPawn = 0; opPawn < PAWNS_PER_PLAYER; opPawn++) {
                                if (pawnPositions[op][opPawn] == actualPos) {
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
    }

    /**
     * Converts a pawn position into a friendly description for menu prompts.
     */
    static String getPositionDescription(int player, int pos) {
        if (pos == -1) return "At Start";
        if (pos >= BOARD_SIZE + HOME_STRETCH_SIZE) return "HOME! ✓";
        if (pos >= BOARD_SIZE) return "Home stretch position " + (pos - BOARD_SIZE);
        return "Board position " + pos;
    }

    /**
     * Checks if every pawn owned by the player has made it to or past the end
     * of the home stretch.
     */
    static boolean checkWin(int player) {
        for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
            if (pawnPositions[player][pawn] < BOARD_SIZE + HOME_STRETCH_SIZE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Advances the turn marker to the next player in order.
     */
    static void nextPlayer() {
        currentPlayer = (currentPlayer + 1) % PLAYERS;
    }

    /**
     * Prints a quick summary of where every pawn sits so players can plan their
     * next moves.
     */
    static void displayBoard() {
        System.out.println("\n=== BOARD STATUS ===");
        for (int player = 0; player < PLAYERS; player++) {
            System.out.print(PLAYER_COLORS[player] + ": ");
            for (int pawn = 0; pawn < PAWNS_PER_PLAYER; pawn++) {
                System.out.print("[" + getPositionDescription(player, pawnPositions[player][pawn]) + "] ");
            }
            System.out.println();
        }
        System.out.println("====================\n");
    }

    /**
     * Shows the core game rules at the start so everyone knows how their cards
     * behave.
     */
    static void printRules() {
        System.out.println("\n=== SORRY! RULES ===");
        System.out.println("Goal: Move all 4 pawns from Start to Home.");
        System.out.println("Draw cards to move. Land on opponents to send them back to Start!");
        System.out.println("Cards: 1,2 = Start or move | 3,5,8,12 = Forward | 4 = Backward");
        System.out.println("       7 = Split move | 10 = Forward 10 or Back 1 | 11 = Switch");
        System.out.println("       Sorry! = Bump opponent with pawn from Start");
        System.out.println("====================\n");
    }
}
