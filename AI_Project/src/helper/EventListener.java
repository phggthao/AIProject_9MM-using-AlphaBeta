package helper;

import board.Piece;

public interface EventListener {
	boolean movedWhitePiece(Piece movedPiece, int newPosition);
	void removedBlackPiece(int piecePosition);
}
