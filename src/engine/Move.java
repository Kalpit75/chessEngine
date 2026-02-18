package engine;

public class Move {
    public int from;
    public int to;

    //special move flags
    public boolean isPromotion;
    public char promotionPiece;

    public boolean isCastling;
    public boolean isEnPassant;
    public char capturedPiece;

    public Move(int from, int to, char capturedPiece){
       this.from = from;
       this.to = to;
       this.capturedPiece = capturedPiece;
    }

    public void setPromotion(char piece){
        this.isPromotion = true;
        this.promotionPiece = piece;
    }

    public void  setCastling(){
        this.isCastling = true;
    }

    public void setEnPassant(){
        this.isEnPassant = true;
    }


    //debug helper, print moves for debug
    @Override
    public String toString() {
        String fromSquare = squareToString(from);
        String toSquare = squareToString(to);
        String result = fromSquare + "-" + toSquare;

        if (capturedPiece != '\0' && capturedPiece !='.') result += " captures " + capturedPiece;
        if (isPromotion) result += " promotes to " + promotionPiece;
        if (isCastling) result += " (castling)";
        if (isEnPassant) result += " (en passant)";

        return result;
    }

    private String squareToString(int square) {
        int file = square % 8;
        int rank = square / 8;
        return "" + (char)('a' + file) + (rank + 1);
    }


    public boolean isCapture() {
        return capturedPiece != '\0' && capturedPiece != '.';
    }
}








