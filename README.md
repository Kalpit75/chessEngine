# Chess Engine with Bitboard Representation

A complete chess engine built in Java from scratch, featuring bitboard representation,
minimax search with alpha-beta pruning, and a Swing-based GUI.

## Features

- **Bitboard Representation**: 12 separate 64-bit bitboards for efficient piece storage
- **Complete Move Generation**: All pieces, special moves (castling, en passant, promotion)
- **Legal Move Validation**: Handles pins, checks, and checkmate detection
- **AI Opponent**: Minimax search with alpha-beta pruning (configurable depth)
- **GUI**: Interactive chess board with piece images and move highlighting
- **FEN Support**: Load any position from FEN notation

## Technical Highlights

- Precomputed attack tables for knights and kings
- Sliding piece move generation with ray-casting
- Position evaluation with material counting
- Make/unmake move system for search

## How to Run

1. Clone the repository
2. Open in your Java IDE
3. Run `gui/BoardPanel.java`
4. Play as white against the AI (black)

## Project Structure
```
src/
├── engine/
│   ├── Position.java          # Bitboard position representation
│   ├── Move.java              # Move representation
│   ├── MoveGenerator.java     # Move generation logic
│   ├── Evaluator.java         # Position evaluation
│   ├── Search.java            # Minimax + alpha-beta
│   └── GameState.java         # State snapshot for unmake
└── gui/
    ├── BoardPanel.java        # Chess GUI
    └── resources/
        ├── images/            # Piece images
        └── sounds/            # Move sounds

