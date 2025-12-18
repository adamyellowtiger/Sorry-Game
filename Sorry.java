public class Sorry {
    /*
    1: You can move a pawn from the start or move forward 1 space.
2: You can move a pawn from the start or move forward 2 spaces. You must draw again.
3: Move forward 3 spaces.
4: Move backward 4 spaces.
5: Move forward 5 spaces.
7: Move forward 7 spaces or split between 2 pawns (e.g. 3 spaces for one pawn, 4 spaces for another).
8: Move forward 8 spaces.
10: Move forward 10 spaces or move backward 1 space.
11: Move forward 11 spaces or switch places with an opponent. If it is impossible to move forward 11 spaces, and there are no opponent pawns on the board, then you will have to switch places with your partner or forfeit your turn.
12: Move forward 12 spaces.
Sorry!: You can save the card for later use, or you can use it to bump an opponent's pawn to the start.
     */
    static final int PLAYERS = 4;
    static final int TRACK_LENGTH = 60;
    
    static int[] cards = {1,2,3,4,5,6,7,8,9,10,11,12,13};
    public static void main(String[] args) {
        System.out.println("This game requires four players. Please ensure that you have four players to play");

    }
}

