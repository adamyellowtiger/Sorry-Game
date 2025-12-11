#!/usr/bin/env python3
"""
Automated demo of Sorry! game
This script demonstrates the game by simulating automated moves.
"""

from sorry_game import *
import random

# Set seed for reproducibility
random.seed(42)

def auto_play_card(game, card, player):
    """Automatically play a card with simple logic."""
    
    if card.type in [CardType.ONE, CardType.TWO]:
        # Prefer starting a new pawn
        pawns_in_start = player.get_pawns_in_start()
        if pawns_in_start:
            game.start_pawn(pawns_in_start[0])
            return True
        # Otherwise move existing pawn
        movable = player.get_movable_pawns()
        if movable:
            for pawn in movable:
                if game.move_pawn(pawn, card.type.value, False):
                    return True
    
    elif card.type == CardType.FOUR:
        # Move backward
        movable = player.get_movable_pawns()
        if movable:
            for pawn in movable:
                if game.move_pawn(pawn, 4, True):
                    return True
    
    elif card.type == CardType.TEN:
        # Try moving forward first
        movable = player.get_movable_pawns()
        if movable:
            for pawn in movable:
                if game.move_pawn(pawn, 10, False):
                    return True
            # Try backward
            for pawn in movable:
                if game.move_pawn(pawn, 1, True):
                    return True
    
    elif card.type == CardType.SORRY:
        # Try to use Sorry! card
        pawns_in_start = player.get_pawns_in_start()
        if pawns_in_start:
            # Find opponent pawns
            for p in game.players:
                if p.color != player.color:
                    opponent_pawns = [pawn for pawn in p.get_movable_pawns() 
                                    if not pawn.in_safe_zone and pawn.position >= 0]
                    if opponent_pawns:
                        opponent_pawn = opponent_pawns[0]
                        my_pawn = pawns_in_start[0]
                        opp_pos = opponent_pawn.position
                        game.send_pawn_to_start(opponent_pawn)
                        game.board.place_pawn(my_pawn, opp_pos)
                        return True
    
    else:
        # Regular move
        movable = player.get_movable_pawns()
        if movable:
            steps = card.type.value
            for pawn in movable:
                if game.move_pawn(pawn, steps, False):
                    return True
    
    return False


def main():
    print("="*60)
    print("AUTOMATED SORRY! GAME DEMO")
    print("="*60)
    
    game = SorryGame(2)
    
    print(f"\nPlayers: {', '.join(p.color.value for p in game.players)}")
    print("Objective: Be the first to get all 4 pawns home!\n")
    
    turn_count = 0
    max_turns = 200  # Prevent infinite games
    
    while turn_count < max_turns:
        player = game.get_current_player()
        
        # Every 10 turns, show board state
        if turn_count % 10 == 0:
            game.display_board()
        
        # Draw and play card
        card = game.deck.draw()
        
        if turn_count % 10 == 0:
            print(f"\nTurn {turn_count + 1}: {player.color.value} draws {card}")
        
        if auto_play_card(game, card, player):
            if turn_count % 10 == 0:
                print(f"  → Move completed!")
        else:
            if turn_count % 10 == 0:
                print(f"  → No valid moves")
        
        game.deck.discard(card)
        
        # Check win
        if player.has_won():
            game.display_board()
            print("\n" + "="*60)
            print(f"🎉 {player.color.value} Player WINS after {turn_count + 1} turns! 🎉")
            print("="*60)
            break
        
        game.next_player()
        turn_count += 1
    
    if turn_count >= max_turns:
        print(f"\nGame ended after {max_turns} turns (demo limit)")
        game.display_board()
    
    print("\n✓ Demo completed successfully!")


if __name__ == "__main__":
    main()
