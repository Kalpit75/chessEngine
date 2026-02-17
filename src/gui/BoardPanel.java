package gui;

import engine.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class BoardPanel extends JPanel {
    private static final int TILE_SIZE = 80;
    private static final Color LIGHT = new Color(240, 217, 181);
    private static final Color DARK = new Color(181, 136, 99);
    private static final Color SELECTED = new Color(255, 255, 0, 120);
    private static final Color HIGHLIGHT = new Color(0, 255, 0, 80);

    private Position position;
    private int selectedRow = -1, selectedCol = -1;
    private List<Move> legalMoves;
    private boolean playingAgainstAI = true;
    private boolean playerIsWhite = true;
    private final Map<Character, Image> pieceImages = new HashMap<>();

    public BoardPanel() {
        setPreferredSize(new Dimension(8 * TILE_SIZE, 8 * TILE_SIZE));
        position = new Position("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
        loadPieceImages();
        initMouse();
    }



    private void loadPieceImages() {
        try {
            pieceImages.put('K', ImageIO.read(getClass().getResource("/gui/resources/images/wk.png")));
            pieceImages.put('Q', ImageIO.read(getClass().getResource("/gui/resources/images/wq.png")));
            pieceImages.put('R', ImageIO.read(getClass().getResource("/gui/resources/images/wr.png")));
            pieceImages.put('B', ImageIO.read(getClass().getResource("/gui/resources/images/wb.png")));
            pieceImages.put('N', ImageIO.read(getClass().getResource("/gui/resources/images/wn.png")));
            pieceImages.put('P', ImageIO.read(getClass().getResource("/gui/resources/images/wp.png")));

            pieceImages.put('k', ImageIO.read(getClass().getResource("/gui/resources/images/bk.png")));
            pieceImages.put('q', ImageIO.read(getClass().getResource("/gui/resources/images/bq.png")));
            pieceImages.put('r', ImageIO.read(getClass().getResource("/gui/resources/images/br.png")));
            pieceImages.put('b', ImageIO.read(getClass().getResource("/gui/resources/images/bb.png")));
            pieceImages.put('n', ImageIO.read(getClass().getResource("/gui/resources/images/bn.png")));
            pieceImages.put('p', ImageIO.read(getClass().getResource("/gui/resources/images/bp.png")));

            System.out.println("Images loaded successfully!");
        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void initMouse() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = e.getX() / TILE_SIZE;
                int row = e.getY() / TILE_SIZE;
                if (!inBounds(row, col)) return;

                handleClick(row, col);
            }
        });
    }

    private void handleClick(int row, int col) {
        // If playing against AI, and it's AI's turn, ignore clicks
        if (playingAgainstAI && position.isWhiteTurn != playerIsWhite) {
            return;
        }

        int square = (7 - row) * 8 + col;

        if (selectedRow == -1) {
            // Select piece
            char piece = position.getPieceAt(square);
            if (piece != '.' && isOurPiece(piece)) {
                selectedRow = row;
                selectedCol = col;
                highlightLegalMoves();
                repaint();
            }
        } else {
            // Try to move
            int fromSquare = (7 - selectedRow) * 8 + selectedCol;
            Move selectedMove = findMove(fromSquare, square);

            if (selectedMove != null) {
                makeMove(selectedMove);
            }else{
                // If invalid move, just clear selection
                selectedRow = -1;
                selectedCol = -1;
                legalMoves = null;
                repaint();
            }
        }
    }

    private boolean isOurPiece(char piece) {
        if (position.isWhiteTurn) {
            return Character.isUpperCase(piece);
        } else {
            return Character.isLowerCase(piece);
        }
    }

    private void highlightLegalMoves() {
        legalMoves = MoveGenerator.generateLegalMoves(position);
        int fromSquare = (7 - selectedRow) * 8 + selectedCol;

        // Filter to only moves from selected square
        legalMoves.removeIf(m -> m.from != fromSquare);
    }

    private Move findMove(int from, int to) {
        List<Move> allMoves = MoveGenerator.generateLegalMoves(position);
        for (Move m : allMoves) {
            if (m.from == from && m.to == to) {
                // Handle promotion - for now just promote to queen
                if (m.isPromotion) {
                    m.setPromotion(position.isWhiteTurn ? 'Q' : 'q');
                }
                return m;
            }
        }
        return null;
    }

    private void makeMove(Move move) {
        position.makeMove(move);
        // Clear highlights immediately
        selectedRow = -1;
        selectedCol = -1;
        legalMoves = null;
        repaint();

        // Check for game over
        if (MoveGenerator.generateLegalMoves(position).isEmpty()) {
            if (position.isKingInCheck(position.isWhiteTurn)) {
                JOptionPane.showMessageDialog(this, "Checkmate!");
            } else {
                JOptionPane.showMessageDialog(this, "Stalemate!");
            }
            return;
        }

        // AI move
        if (playingAgainstAI && position.isWhiteTurn != playerIsWhite) {
            makeAIMove();
        }
    }



    private void makeAIMove() {

        // Make a copy of the position for search!
        final String currentFEN = position.toFEN();

        SwingWorker<Move, Void> worker = new SwingWorker<>() {
            @Override
            protected Move doInBackground() {
                // Search on a COPY, not the GUI's position!
                Position searchPosition = new Position(currentFEN);
                return Search.findBestMove(searchPosition, 4);
            }

            @Override
            protected void done() {
                try {
                    Move aiMove = get();
                    if (aiMove != null) {
                        position.makeMove(aiMove);  // Only modify GUI position once

                        legalMoves = null;
                        selectedRow = -1;
                        selectedCol = -1;

                        repaint();

                        if (MoveGenerator.generateLegalMoves(position).isEmpty()) {
                            if (position.isKingInCheck(position.isWhiteTurn)) {
                                JOptionPane.showMessageDialog(BoardPanel.this, "Checkmate!");
                            } else {
                                JOptionPane.showMessageDialog(BoardPanel.this, "Stalemate!");
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawHighlights(g);
        drawPieces(g);
    }

    private void drawBoard(Graphics g) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                g.setColor((r + c) % 2 == 0 ? LIGHT : DARK);
                g.fillRect(c * TILE_SIZE, r * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }


    private void drawHighlights(Graphics g) {
        // Don't draw highlights during AI's turn
        if (playingAgainstAI && position.isWhiteTurn != playerIsWhite) {
            legalMoves = null;
            return;
        }

        if (selectedRow != -1) {
            g.setColor(SELECTED);
            g.fillRect(selectedCol * TILE_SIZE, selectedRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        if (legalMoves != null) {
            g.setColor(HIGHLIGHT);
            for (Move m : legalMoves) {
                int row = 7 - (m.to / 8);
                int col = m.to % 8;
                g.fillOval(col * TILE_SIZE + TILE_SIZE/3, row * TILE_SIZE + TILE_SIZE/3,
                        TILE_SIZE/3, TILE_SIZE/3);
            }
        }
    }


    private void drawPieces(Graphics g) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                int square = (7 - r) * 8 + c;
                char piece = position.getPieceAt(square);
                if (piece != '.' && pieceImages.containsKey(piece)) {
                    Image img = pieceImages.get(piece);
                    g.drawImage(img, c * TILE_SIZE + 8, r * TILE_SIZE + 8,
                            TILE_SIZE - 16, TILE_SIZE - 16, null);
                }
            }
        }
    }


    private boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Chess Engine");
        BoardPanel board = new BoardPanel();
        frame.add(board);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}