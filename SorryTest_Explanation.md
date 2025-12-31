## SorryTest.java walkthrough

Below is a plain-English description of what each function does in `SorryTest.java`. The tone is meant to feel like a capable high-school programmer explaining their code to classmates.

- `next()`: Pulls the next token from the input stream, reloading from a new console line if it ran out of tokens. It keeps the game from choking when players type commands with spaces.
- `nextInt()`: Uses `next()` but converts the token to an integer so menu choices and card selections work smoothly.
- `main(String[] args)`: Prints the welcome text, initializes the board and deck, shows the rules, then loops through player turns until someone wins. It handles saved Sorry! cards, card drawing, and deciding whether a player gets another turn.
- `initializeGame()`: Resets every pawn back to Start, clears out any saved Sorry! cards, and shuffles a new deck so the round begins fresh.
- `resetDeck()`: Refills the deck with four copies of each allowed card (skipping 6 and 9) and four Sorry! cards, then shuffles them.
- `drawCard()`: Grabs a card off the top of the deck, rebuilding and reshuffling the pile if it was empty.
- `getCardName(int card)`: Turns the numeric card ID into a readable label, switching 13 into “Sorry!”.
- `printCardAction(int card)`: Prints a short reminder of what the drawn card lets you do.
- `playCard(int player, int card)`: Routes the card to its specific handler and returns true only when card 2 says the player draws again.
- `playCard1(int player)`: Lets the player either leave Start or move a pawn forward one space.
- `playCard2(int player)`: Same vibe as card 1 but the player earns an extra draw afterward.
- `playCard7(int player)`: Either moves one pawn seven spaces or splits the seven between two pawns based on player input.
- `playCard10(int player)`: Gives the choice to sprint forward ten or step back one.
- `playCard11(int player)`: Either moves forward eleven or swaps positions with an opponent’s pawn.
- `playCard13(int player)`: Handles the Sorry! card, letting players use it immediately or stash it for later.
- `playSorryCard(int player)`: Boots a chosen opponent pawn on the main track back to Start and replaces it with one of the player’s Start pawns.
- `moveFromStart(int player)`: Moves the first available pawn out of Start onto the player’s starting tile and bumps opponents if needed.
- `moveForward(int player, int spaces)`: Lets the player pick a pawn and push it forward the requested number of spaces.
- `moveBackward(int player, int spaces)`: Lets the player pick a pawn and walk it backward, used by cards like 4 and the back option on 10.
- `selectPawn(int player, boolean onBoardOnly)`: Shows where each pawn is, filters out illegal choices if needed, and returns the chosen pawn index or -1 if the pick was invalid.
- `movePawn(int player, int pawn, int spaces, boolean forward)`: Handles all the movement math, including entering the home stretch, checking for blocked spots, bumping opponents, and triggering slides.
- `switchWithOpponent(int player)`: Swaps a selected pawn with an opponent pawn that sits on the main track.
- `isOwnPawnAt(int player, int pos)`: Checks if one of the player’s pawns already occupies the target position.
- `checkBump(int player, int pawn)`: Sends any opponent pawn sharing the landing spot back to Start.
- `checkSlide(int player, int pawn)`: Applies slide rules when landing on another color’s slide start, moving forward and bumping anyone on the slide path.
- `getPositionDescription(int player, int pos)`: Converts a pawn’s position into a friendly status string for menus.
- `checkWin(int player)`: Verifies whether all of a player’s pawns have reached or passed the end of the home stretch.
- `nextPlayer()`: Advances the turn marker to the next player in order.
- `displayBoard()`: Prints a snapshot of where every pawn currently sits.
- `printRules()`: Shows the core game rules so everyone remembers what the cards do.
