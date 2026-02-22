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
    private static final String IMAGE_DIR = "/gui/resources/images/";
    private static final Color LAST_MOVE = new Color(215, 183, 17, 150); // olive green tint

    private int selectedRow = -1, selectedCol = -1;
    private List<Move> allLegalMoves;
    private final Map<Character, Image> pieceImages = new HashMap<>();
    private GameController controller;


    public BoardPanel(boolean playerIsWhite) {
        controller = new GameController(playerIsWhite, () -> {
            selectedRow = -1;
            selectedCol = -1;
            allLegalMoves = null;
            repaint();
            String gameStatus = controller.isGameOver();
            if (gameStatus != null) {
                JOptionPane.showMessageDialog(this, gameStatus);
            }
        });
        setPreferredSize(new Dimension(8 * TILE_SIZE, 8 * TILE_SIZE));
        loadPieceImages();
        initMouse();
    }

    private int toScreenRow(int square){
        if (controller.isPlayerWhite()){
            return 7 - (square / 8);
        }else {
            return square / 8;
        }
    }

    private int toScreenCol(int square){
        if (controller.isPlayerWhite()){
            return square % 8;
        } else {
            return 7 - (square % 8);
        }
    }

    private int toSquare(int row, int col){
        if (controller.isPlayerWhite()){
            return (7 - row) * 8 + col;
        } else {
            return row * 8 + (7 - col);
        }
    }



    private void loadPieceImages() {
        try {
            char[] pieces = {'K','k', 'Q', 'q','R', 'r', 'N', 'n', 'P', 'p', 'B', 'b'};
            for (char piece : pieces){
                String colorPrefix;
                if (Character.isUpperCase(piece)) {
                    colorPrefix = "w";
                } else {
                    colorPrefix = "b";
                }
                String fileName = IMAGE_DIR + colorPrefix + Character.toLowerCase(piece) + ".png";
                pieceImages.put(piece, ImageIO.read(getClass().getResource(fileName)));
            }
            System.out.println("Images loaded successfully!");
        } catch (Exception e) {
            System.out.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void initMouse() {
        // If player is Black, AI needs to move first
        if (!controller.isPlayerWhite()) {
            controller.makeAIMove();
        }

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
        if (!controller.isPlayerTurn()) {
            return;
        }

        int square = toSquare(row, col);

        if (selectedRow == -1) {
            // Select piece
            char piece = controller.getPosition().getPieceAt(square);
            if (piece != '.' && isOurPiece(piece)) {
                selectedRow = row;
                selectedCol = col;
                highlightLegalMoves();
                repaint();
            }
        } else {
            // Try to move
            int fromSquare = toSquare(selectedRow, selectedCol);
            Move selectedMove = findMove(fromSquare, square, allLegalMoves);

            if (selectedMove != null) {
                controller.makeMove(selectedMove);
            }else{
                // If invalid move, just clear selection
                selectedRow = -1;
                selectedCol = -1;
                allLegalMoves = null;
                repaint();
            }
        }
    }

    private boolean isOurPiece(char piece) {
        if (controller.getPosition().isWhiteTurn) {
            return Character.isUpperCase(piece);
        } else {
            return Character.isLowerCase(piece);
        }
    }

    private void highlightLegalMoves() {
        allLegalMoves = MoveGenerator.generateLegalMoves(controller.getPosition());
        int fromSquare = toSquare(selectedRow, selectedCol);

        // Filter to only moves from selected square
        allLegalMoves.removeIf(m -> m.from != fromSquare);
    }



    private Move findMove(int from, int to, List<Move> allLegalMoves) {
        for (Move m : allLegalMoves) {
            if (m.from == from && m.to == to) {
                // Handle promotion - for now just promote to queen
                if (m.isPromotion) {
                    m.setPromotion(controller.getPosition().isWhiteTurn ? 'Q' : 'q');
                }
                return m;
            }
        }
        return null;
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
        // Draw last move highlight
        if (controller.getLastMove() != null) {
            g.setColor(LAST_MOVE);
            int fromRow = toScreenRow(controller.getLastMove().from);
            int fromCol = toScreenCol(controller.getLastMove().from);
            int toRow   = toScreenRow(controller.getLastMove().to);
            int toCol   = toScreenCol(controller.getLastMove().to);
            g.fillRect(fromCol * TILE_SIZE, fromRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            g.fillRect(toCol   * TILE_SIZE, toRow   * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        // Don't draw highlights during AI's turn
        if (!controller.isPlayerTurn()) {
            allLegalMoves = null;
            return;
        }

        if (selectedRow != -1) {
            g.setColor(SELECTED);
            g.fillRect(selectedCol * TILE_SIZE, selectedRow * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }

        if (allLegalMoves != null) {
            g.setColor(HIGHLIGHT);
            for (Move m : allLegalMoves) {
                int row = toScreenRow(m.to);
                int col = toScreenCol(m.to);
                g.fillOval(col * TILE_SIZE + TILE_SIZE/3, row * TILE_SIZE + TILE_SIZE/3,
                        TILE_SIZE/3, TILE_SIZE/3);
            }
        }
    }


    private void drawPieces(Graphics g) {
        for (int square = 0; square < 64; square++){
            int row = toScreenRow(square);
            int col = toScreenCol(square);
            char piece = controller.getPosition().getPieceAt(square);
            if (piece != '.' && pieceImages.containsKey(piece)) {
                Image img = pieceImages.get(piece);
                g.drawImage(img, col * TILE_SIZE + 8, row * TILE_SIZE + 8,
                        TILE_SIZE - 16, TILE_SIZE - 16, null);
            }
        }
    }


    private boolean inBounds(int r, int c) {
        return r >= 0 && r < 8 && c >= 0 && c < 8;
    }

}