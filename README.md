# Sorry-Game

A console-based implementation of the classic Sorry! board game.

## About

Sorry! is a board game where 2-4 players race their pawns around the board from Start to Home. Players draw cards to move their pawns, and special cards provide unique abilities like bumping opponents back to Start or swapping positions.

## Features

- **2-4 Players**: Play with 2, 3, or 4 players
- **Console Interface**: Simple text-based interface for easy gameplay
- **Classic Cards**: Implementation of standard Sorry! cards (1, 2, 3, 4, 5, 7, 8, 10, 11, 12, Sorry!)
- **Game Mechanics**:
  - 60-space main track
  - Safe zones for each player
  - Start and Home areas
  - Bumping opponents back to Start
  - Special card effects

## How to Play

### Requirements

- Python 3.6 or higher

### Running the Game

```bash
python3 sorry_game.py
```

### Running the Demo

To see an automated demonstration of the game in action:

```bash
python3 demo.py
```

This will run an automated game where simple AI makes moves, showing how the game progresses.

### Game Rules

1. **Objective**: Be the first player to get all 4 pawns from Start to Home

2. **Turn Structure**:
   - Draw a card
   - Choose a pawn to move (if possible)
   - Execute the move
   - Next player's turn

3. **Card Effects**:
   - **1 or 2**: Start a pawn OR move forward 1/2 spaces
   - **3**: Move forward 3 spaces
   - **4**: Move backward 4 spaces
   - **5**: Move forward 5 spaces
   - **7**: Move forward 7 spaces (can split in full version)
   - **8**: Move forward 8 spaces
   - **10**: Move forward 10 OR backward 1 space
   - **11**: Move forward 11 spaces (can swap with opponent in full version)
   - **12**: Move forward 12 spaces
   - **Sorry!**: Start a pawn and swap positions with an opponent

4. **Special Mechanics**:
   - **Starting**: Use a 1 or 2 card to move a pawn from Start to the board
   - **Bumping**: Landing on an opponent's pawn sends it back to Start
   - **Safe Zones**: Only your pawns can enter your safe zone
   - **Home**: Exactly reach Home to score a pawn (no overshooting)

### Tips

- Plan your moves carefully - sometimes it's better to bump an opponent than to move forward
- The Sorry! card is powerful - use it strategically
- You must get all 4 pawns home to win

## Game Design

The game is implemented in Python with the following structure:

- **Pawn**: Represents individual game pieces
- **Player**: Manages a player's 4 pawns
- **Board**: Maintains the game state (main track, safe zones, homes)
- **Deck**: Manages the card deck and discard pile
- **SorryGame**: Main game controller with turn logic

## Future Enhancements

Potential improvements for future versions:

- Graphical interface
- AI players
- Full implementation of split moves (card 7)
- Full implementation of swap moves (card 11)
- Network multiplayer
- Save/load game state
- Animated movements
- Sound effects

## License

This is a personal project implementation of the Sorry! game for educational purposes.

## Contributing

Feel free to fork and submit pull requests for improvements!