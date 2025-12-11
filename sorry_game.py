#!/usr/bin/env python3
"""
Console-based Sorry! Game
A simplified implementation of the classic board game.
"""

import random
from enum import Enum
from typing import List, Optional, Tuple


class Color(Enum):
    """Player colors."""
    RED = "Red"
    BLUE = "Blue"
    YELLOW = "Yellow"
    GREEN = "Green"


class CardType(Enum):
    """Card types in the deck."""
    ONE = 1
    TWO = 2
    THREE = 3
    FOUR = 4
    FIVE = 5
    SEVEN = 7
    EIGHT = 8
    TEN = 10
    ELEVEN = 11
    TWELVE = 12
    SORRY = "Sorry!"


class Pawn:
    """Represents a pawn on the board."""
    
    def __init__(self, player_color: Color, pawn_id: int):
        self.color = player_color
        self.id = pawn_id
        self.position = -1  # -1 means in Start area
        self.in_home = False
        self.in_safe_zone = False
    
    def __repr__(self):
        return f"{self.color.value[0]}{self.id}"


class Card:
    """Represents a card in the deck."""
    
    def __init__(self, card_type: CardType):
        self.type = card_type
    
    def __repr__(self):
        return str(self.type.value)


class Deck:
    """Manages the card deck."""
    
    def __init__(self):
        self.cards = []
        self.discard_pile = []
        self._initialize_deck()
    
    def _initialize_deck(self):
        """Create a standard Sorry! deck."""
        # Standard Sorry! deck distribution
        card_counts = {
            CardType.ONE: 5,
            CardType.TWO: 4,
            CardType.THREE: 4,
            CardType.FOUR: 4,
            CardType.FIVE: 4,
            CardType.SEVEN: 4,
            CardType.EIGHT: 4,
            CardType.TEN: 4,
            CardType.ELEVEN: 4,
            CardType.TWELVE: 4,
            CardType.SORRY: 4,
        }
        
        for card_type, count in card_counts.items():
            self.cards.extend([Card(card_type) for _ in range(count)])
        
        random.shuffle(self.cards)
    
    def draw(self) -> Card:
        """Draw a card from the deck."""
        if not self.cards:
            # Reshuffle discard pile
            self.cards = self.discard_pile
            self.discard_pile = []
            random.shuffle(self.cards)
        
        return self.cards.pop()
    
    def discard(self, card: Card):
        """Add card to discard pile."""
        self.discard_pile.append(card)


class Board:
    """Represents the game board."""
    
    BOARD_SIZE = 60
    PAWNS_PER_PLAYER = 4
    SAFE_ZONE_LENGTH = 5
    
    def __init__(self, num_players: int):
        self.num_players = num_players
        # Main track: index 0-59 (circular)
        self.main_track = [[] for _ in range(self.BOARD_SIZE)]
        # Safe zones for each player
        self.safe_zones = {color: [[] for _ in range(self.SAFE_ZONE_LENGTH)] 
                          for color in list(Color)[:num_players]}
        # Home for each player
        self.homes = {color: [] for color in list(Color)[:num_players]}
        # Start positions for each color
        self.start_positions = {
            Color.RED: 0,
            Color.BLUE: 15,
            Color.YELLOW: 30,
            Color.GREEN: 45
        }
        # Safe zone entries
        self.safe_zone_entries = {
            Color.RED: 2,
            Color.BLUE: 17,
            Color.YELLOW: 32,
            Color.GREEN: 47
        }
    
    def get_pawn_at_position(self, position: int) -> Optional[Pawn]:
        """Get pawn at a specific position on main track."""
        if 0 <= position < self.BOARD_SIZE:
            if self.main_track[position]:
                return self.main_track[position][0]
        return None
    
    def place_pawn(self, pawn: Pawn, position: int):
        """Place a pawn on the board."""
        # Remove pawn from current position
        self.remove_pawn(pawn)
        
        # Place on new position
        if 0 <= position < self.BOARD_SIZE:
            pawn.position = position
            pawn.in_safe_zone = False
            self.main_track[position].append(pawn)
    
    def remove_pawn(self, pawn: Pawn):
        """Remove pawn from current position."""
        if pawn.in_home:
            if pawn in self.homes[pawn.color]:
                self.homes[pawn.color].remove(pawn)
        elif pawn.in_safe_zone:
            for i, space in enumerate(self.safe_zones[pawn.color]):
                if pawn in space:
                    self.safe_zones[pawn.color][i].remove(pawn)
        elif pawn.position >= 0:
            if pawn in self.main_track[pawn.position]:
                self.main_track[pawn.position].remove(pawn)
    
    def move_to_safe_zone(self, pawn: Pawn, safe_position: int):
        """Move pawn to safe zone."""
        self.remove_pawn(pawn)
        pawn.in_safe_zone = True
        pawn.position = safe_position
        self.safe_zones[pawn.color][safe_position].append(pawn)
    
    def move_to_home(self, pawn: Pawn):
        """Move pawn to home."""
        self.remove_pawn(pawn)
        pawn.in_home = True
        pawn.position = -2  # Special value for home
        self.homes[pawn.color].append(pawn)


class Player:
    """Represents a player."""
    
    def __init__(self, color: Color):
        self.color = color
        self.pawns = [Pawn(color, i) for i in range(Board.PAWNS_PER_PLAYER)]
    
    def has_won(self) -> bool:
        """Check if player has won."""
        return all(pawn.in_home for pawn in self.pawns)
    
    def get_movable_pawns(self) -> List[Pawn]:
        """Get pawns that are on the board."""
        return [p for p in self.pawns if p.position >= 0 or p.in_safe_zone]
    
    def get_pawns_in_start(self) -> List[Pawn]:
        """Get pawns still in start area."""
        return [p for p in self.pawns if p.position == -1 and not p.in_home]


class SorryGame:
    """Main game class."""
    
    def __init__(self, num_players: int = 2):
        if num_players < 2 or num_players > 4:
            raise ValueError("Number of players must be between 2 and 4")
        
        self.num_players = num_players
        self.board = Board(num_players)
        self.deck = Deck()
        self.players = [Player(color) for color in list(Color)[:num_players]]
        self.current_player_idx = 0
    
    def get_current_player(self) -> Player:
        """Get the current player."""
        return self.players[self.current_player_idx]
    
    def next_player(self):
        """Move to next player."""
        self.current_player_idx = (self.current_player_idx + 1) % self.num_players
    
    def start_pawn(self, pawn: Pawn):
        """Move a pawn from Start to the board."""
        start_pos = self.board.start_positions[pawn.color]
        # Bump any pawn at start position
        existing_pawn = self.board.get_pawn_at_position(start_pos)
        if existing_pawn:
            self.send_pawn_to_start(existing_pawn)
        self.board.place_pawn(pawn, start_pos)
    
    def send_pawn_to_start(self, pawn: Pawn):
        """Send a pawn back to Start."""
        self.board.remove_pawn(pawn)
        pawn.position = -1
        pawn.in_safe_zone = False
        pawn.in_home = False
    
    def can_move_pawn(self, pawn: Pawn, steps: int, backward: bool = False) -> bool:
        """Check if a pawn can move."""
        if pawn.in_home:
            return False
        
        if pawn.position == -1:  # In start
            return False
        
        new_pos = self._calculate_new_position(pawn, steps, backward)
        return new_pos is not None
    
    def _calculate_new_position(self, pawn: Pawn, steps: int, backward: bool = False) -> Optional[Tuple[str, int]]:
        """Calculate new position. Returns (zone_type, position) or None if invalid."""
        if pawn.in_safe_zone:
            # Already in safe zone
            if backward:
                return None  # Can't move backward in safe zone
            
            new_safe_pos = pawn.position + steps
            if new_safe_pos == Board.SAFE_ZONE_LENGTH:
                return ("home", 0)
            elif new_safe_pos < Board.SAFE_ZONE_LENGTH:
                return ("safe", new_safe_pos)
            else:
                return None  # Overshoot
        
        # On main track
        current_pos = pawn.position
        
        if backward:
            new_pos = (current_pos - steps) % Board.BOARD_SIZE
            return ("main", new_pos)
        else:
            # Check if we pass safe zone entry
            safe_entry = self.board.safe_zone_entries[pawn.color]
            new_pos = current_pos + steps
            
            # Check if we cross or land on safe zone entry
            positions_crossed = [(current_pos + i) % Board.BOARD_SIZE for i in range(1, steps + 1)]
            
            if safe_entry in positions_crossed:
                # Calculate position in safe zone
                steps_to_entry = 0
                pos = current_pos
                while pos != safe_entry:
                    pos = (pos + 1) % Board.BOARD_SIZE
                    steps_to_entry += 1
                
                steps_in_safe = steps - steps_to_entry
                if steps_in_safe == Board.SAFE_ZONE_LENGTH:
                    return ("home", 0)
                elif steps_in_safe < Board.SAFE_ZONE_LENGTH:
                    return ("safe", steps_in_safe)
                else:
                    return None  # Overshoot
            else:
                # Stay on main track
                final_pos = new_pos % Board.BOARD_SIZE
                return ("main", final_pos)
    
    def move_pawn(self, pawn: Pawn, steps: int, backward: bool = False) -> bool:
        """Move a pawn. Returns True if successful."""
        result = self._calculate_new_position(pawn, steps, backward)
        
        if result is None:
            return False
        
        zone_type, position = result
        
        if zone_type == "home":
            self.board.move_to_home(pawn)
            return True
        elif zone_type == "safe":
            # Check if space is occupied
            if self.board.safe_zones[pawn.color][position]:
                return False  # Can't move to occupied safe space
            self.board.move_to_safe_zone(pawn, position)
            return True
        else:  # main track
            # Bump any pawn at destination
            existing_pawn = self.board.get_pawn_at_position(position)
            if existing_pawn and existing_pawn.color != pawn.color:
                self.send_pawn_to_start(existing_pawn)
            elif existing_pawn and existing_pawn.color == pawn.color:
                return False  # Can't land on own pawn
            
            self.board.place_pawn(pawn, position)
            return True
    
    def swap_pawns(self, pawn1: Pawn, pawn2: Pawn) -> bool:
        """Swap positions of two pawns (for Sorry! card)."""
        # Can't swap if in safe zone or home
        if pawn1.in_safe_zone or pawn1.in_home or pawn2.in_safe_zone or pawn2.in_home:
            return False
        
        if pawn1.position == -1 or pawn2.position == -1:
            return False
        
        pos1 = pawn1.position
        pos2 = pawn2.position
        
        self.board.place_pawn(pawn1, pos2)
        self.board.place_pawn(pawn2, pos1)
        
        return True
    
    def handle_card(self, card: Card, player: Player) -> bool:
        """Handle a card play. Returns True if a valid move was made."""
        if card.type == CardType.ONE or card.type == CardType.TWO:
            # Can start a pawn or move forward
            pawns_in_start = player.get_pawns_in_start()
            movable_pawns = player.get_movable_pawns()
            
            if pawns_in_start:
                print(f"\nCard {card.type.value}: Start a pawn or move {card.type.value} forward")
                print("Options:")
                print("1. Start a new pawn")
                if movable_pawns:
                    print(f"2. Move a pawn {card.type.value} spaces forward")
                
                choice = input("Choose (1 or 2): ").strip()
                
                if choice == "1":
                    self.start_pawn(pawns_in_start[0])
                    return True
                elif choice == "2" and movable_pawns:
                    pawn = self._choose_pawn(movable_pawns, card.type.value, False)
                    if pawn and self.move_pawn(pawn, card.type.value, False):
                        return True
            elif movable_pawns:
                pawn = self._choose_pawn(movable_pawns, card.type.value, False)
                if pawn and self.move_pawn(pawn, card.type.value, False):
                    return True
        
        elif card.type == CardType.THREE:
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                pawn = self._choose_pawn(movable_pawns, 3, False)
                if pawn and self.move_pawn(pawn, 3, False):
                    return True
        
        elif card.type == CardType.FOUR:
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                pawn = self._choose_pawn(movable_pawns, 4, True)
                if pawn and self.move_pawn(pawn, 4, True):
                    return True
        
        elif card.type == CardType.FIVE:
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                pawn = self._choose_pawn(movable_pawns, 5, False)
                if pawn and self.move_pawn(pawn, 5, False):
                    return True
        
        elif card.type == CardType.SEVEN:
            # Can split between pawns (simplified: just move one pawn 7)
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                pawn = self._choose_pawn(movable_pawns, 7, False)
                if pawn and self.move_pawn(pawn, 7, False):
                    return True
        
        elif card.type == CardType.EIGHT:
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                pawn = self._choose_pawn(movable_pawns, 8, False)
                if pawn and self.move_pawn(pawn, 8, False):
                    return True
        
        elif card.type == CardType.TEN:
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                print(f"\nCard 10: Move 10 forward or 1 backward")
                print("1. Move 10 forward")
                print("2. Move 1 backward")
                choice = input("Choose (1 or 2): ").strip()
                
                if choice == "1":
                    pawn = self._choose_pawn(movable_pawns, 10, False)
                    if pawn and self.move_pawn(pawn, 10, False):
                        return True
                elif choice == "2":
                    pawn = self._choose_pawn(movable_pawns, 1, True)
                    if pawn and self.move_pawn(pawn, 1, True):
                        return True
        
        elif card.type == CardType.ELEVEN:
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                # Simplified: just move 11 (swap with opponent is complex for console)
                pawn = self._choose_pawn(movable_pawns, 11, False)
                if pawn and self.move_pawn(pawn, 11, False):
                    return True
        
        elif card.type == CardType.TWELVE:
            movable_pawns = player.get_movable_pawns()
            if movable_pawns:
                pawn = self._choose_pawn(movable_pawns, 12, False)
                if pawn and self.move_pawn(pawn, 12, False):
                    return True
        
        elif card.type == CardType.SORRY:
            # Start a pawn and swap with opponent
            pawns_in_start = player.get_pawns_in_start()
            if pawns_in_start:
                print("\nSorry! card: Start a pawn and swap with an opponent")
                # Find opponent pawns on board
                opponent_pawns = []
                for p in self.players:
                    if p.color != player.color:
                        opponent_pawns.extend([pawn for pawn in p.get_movable_pawns() 
                                             if not pawn.in_safe_zone])
                
                if opponent_pawns:
                    print("Opponent pawns:")
                    for i, pawn in enumerate(opponent_pawns):
                        pos_str = f"position {pawn.position}" if pawn.position >= 0 else "start"
                        print(f"{i+1}. {pawn} at {pos_str}")
                    
                    choice = input(f"Choose opponent pawn to swap (1-{len(opponent_pawns)}): ").strip()
                    try:
                        idx = int(choice) - 1
                        if 0 <= idx < len(opponent_pawns):
                            opponent_pawn = opponent_pawns[idx]
                            my_pawn = pawns_in_start[0]
                            
                            # Place my pawn at opponent position
                            opp_pos = opponent_pawn.position
                            self.send_pawn_to_start(opponent_pawn)
                            self.board.place_pawn(my_pawn, opp_pos)
                            return True
                    except ValueError:
                        pass
        
        return False
    
    def _choose_pawn(self, pawns: List[Pawn], steps: int, backward: bool) -> Optional[Pawn]:
        """Let player choose which pawn to move."""
        valid_pawns = [p for p in pawns if self.can_move_pawn(p, steps, backward)]
        
        if not valid_pawns:
            print("No valid moves available!")
            return None
        
        if len(valid_pawns) == 1:
            return valid_pawns[0]
        
        print("\nChoose a pawn to move:")
        for i, pawn in enumerate(valid_pawns):
            if pawn.in_safe_zone:
                print(f"{i+1}. {pawn} in safe zone at position {pawn.position}")
            else:
                print(f"{i+1}. {pawn} at position {pawn.position}")
        
        while True:
            choice = input(f"Choose (1-{len(valid_pawns)}): ").strip()
            try:
                idx = int(choice) - 1
                if 0 <= idx < len(valid_pawns):
                    return valid_pawns[idx]
            except ValueError:
                pass
            print("Invalid choice, try again.")
    
    def display_board(self):
        """Display the current board state."""
        print("\n" + "="*60)
        print("BOARD STATE")
        print("="*60)
        
        for player in self.players:
            print(f"\n{player.color.value} Player:")
            
            # Pawns in start
            in_start = player.get_pawns_in_start()
            if in_start:
                print(f"  In Start: {', '.join(str(p) for p in in_start)}")
            
            # Pawns on main track
            on_board = [p for p in player.pawns if p.position >= 0 and not p.in_safe_zone]
            if on_board:
                for pawn in on_board:
                    print(f"  {pawn} at position {pawn.position}")
            
            # Pawns in safe zone
            in_safe = [p for p in player.pawns if p.in_safe_zone]
            if in_safe:
                for pawn in in_safe:
                    print(f"  {pawn} in safe zone at position {pawn.position}")
            
            # Pawns in home
            in_home = [p for p in player.pawns if p.in_home]
            if in_home:
                print(f"  In Home: {', '.join(str(p) for p in in_home)} ({len(in_home)}/4)")
        
        print("\n" + "="*60)
    
    def play_turn(self) -> bool:
        """Play one turn. Returns True if game continues."""
        player = self.get_current_player()
        
        print(f"\n{'*'*60}")
        print(f"{player.color.value} Player's Turn")
        print(f"{'*'*60}")
        
        # Draw card
        card = self.deck.draw()
        print(f"\nYou drew: {card}")
        
        # Try to play card
        if self.handle_card(card, player):
            print("Move completed!")
        else:
            print("No valid moves available. Turn skipped.")
        
        # Discard card
        self.deck.discard(card)
        
        # Check win condition
        if player.has_won():
            print(f"\n{'='*60}")
            print(f"🎉 {player.color.value} Player WINS! 🎉")
            print(f"{'='*60}")
            return False
        
        # Next player
        self.next_player()
        return True
    
    def play(self):
        """Main game loop."""
        print("="*60)
        print("WELCOME TO SORRY!")
        print("="*60)
        print(f"\nPlayers: {', '.join(p.color.value for p in self.players)}")
        print("\nObjective: Be the first to get all 4 pawns home!")
        print("\nPress Enter to start...")
        input()
        
        turn_count = 0
        while True:
            self.display_board()
            
            if not self.play_turn():
                break
            
            turn_count += 1
            
            if turn_count % self.num_players == 0:
                print("\n--- Press Enter to continue ---")
                input()
        
        print("\nGame Over! Thanks for playing!")


def main():
    """Main entry point."""
    print("="*60)
    print("SORRY! - Console Game")
    print("="*60)
    
    # Get number of players
    while True:
        try:
            num_players = int(input("\nHow many players? (2-4): ").strip())
            if 2 <= num_players <= 4:
                break
            print("Please enter a number between 2 and 4.")
        except ValueError:
            print("Please enter a valid number.")
    
    # Create and start game
    game = SorryGame(num_players)
    game.play()


if __name__ == "__main__":
    main()
