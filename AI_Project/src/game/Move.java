package game;

public class Move {
	public int indexPiece, indexRemovedPiece;
	public int srcIndexOnBoard, destIndexOnBoard, indexRemovedPieceOnBoard;
	public int score = 0;
	
	public Move(int index, int src, int dest, int removePiece, int removeOnBoard) {
		this.indexPiece = index;
		this.srcIndexOnBoard = src;
		this.destIndexOnBoard = dest;
		this.indexRemovedPiece = removePiece;
		this.indexRemovedPieceOnBoard = removeOnBoard;
	}
	
	public Move(Move original) {
		this.indexPiece = original.indexPiece;
		this.srcIndexOnBoard = original.srcIndexOnBoard;
		this.destIndexOnBoard = original.destIndexOnBoard;
		this.indexRemovedPiece = original.indexRemovedPiece;
		this.indexRemovedPieceOnBoard = original.indexRemovedPieceOnBoard;
	}
}