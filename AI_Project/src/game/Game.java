package game;

import board.Board;
import board.BoardController;
import javafx.scene.paint.Color;

public class Game {
	public enum GamePhase {Opening, Middle, Ending, Over};
	public static final int MIN_PIECES_ON_BOARD = 3;
	public static GamePhase currentPhase = GamePhase.Opening;
	
	public static void updateGamePhase(Board board) {
		switch (currentPhase) {
		case Opening:
			if (board.checkAllPiecesOnBoard(Color.WHITE) && board.checkAllPiecesOnBoard(Color.BLACK))
				currentPhase = GamePhase.Middle;
			break;
		case Middle:
			if (board.getNumberOfPiecesOnBoard(Color.WHITE) == MIN_PIECES_ON_BOARD 
					|| board.getNumberOfPiecesOnBoard(Color.BLACK) == MIN_PIECES_ON_BOARD)
				currentPhase = GamePhase.Ending;
			else if(board.stepCnt >= 30)
				currentPhase = GamePhase.Over;
			break;
		case Ending:
			if (board.getNumberOfPiecesOnBoard(Color.WHITE) < MIN_PIECES_ON_BOARD
					|| board.getNumberOfPiecesOnBoard(Color.BLACK) < MIN_PIECES_ON_BOARD
					|| board.stepCnt >= 30)
				currentPhase = GamePhase.Over;
			break;
		default:
			break;
		}
	}

	public static void gameOver(Board board) {
		// Show winner/draw screen
        if(board.stepCnt >= 30){
			BoardController.setGameResultVisibility(true, false, false);
        }
        else if(board.getNumberOfPiecesOnBoard(Color.WHITE) < MIN_PIECES_ON_BOARD){
			BoardController.setGameResultVisibility(false, false, true);
        }
        else{
			BoardController.setGameResultVisibility(false, true, false);
        }
        System.out.println("Game is over~");
	}
}