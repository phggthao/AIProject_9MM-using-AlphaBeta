package board;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import game.AIPlayer;
import game.Game;
import game.Game.GamePhase;
import game.Move;
import helper.EventListener;
import javafx.application.Platform;
import javafx.scene.paint.Color;

public class Board implements EventListener {
	public static final int[][] possibleMills = {
			{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {9, 10, 11}, {12, 13, 14}, {15, 16, 17}, {18, 19, 20}, {21, 22, 23}, 
			{0, 9, 21}, {3, 10, 18}, {6, 11, 15}, {1, 4, 7}, {16, 19, 22}, {8, 12, 17}, {5, 13, 20}, {2, 14, 23}
	};
	public static final int[][] possibleSlides = {
			{1, 9}, {0, 2, 4}, {1, 14}, {4, 10}, {1, 3, 5, 7}, {4, 13}, {7, 11}, {4, 6 ,8}, {7, 12}, {0, 10, 21},
			{3, 9, 11, 18}, {6, 10, 15}, {8, 13, 17}, {5, 12, 14, 20}, {2, 13, 23}, {11, 16}, {15, 17, 19}, {12, 16},
			{10, 19}, {16, 18, 20, 22}, {13, 19}, {9, 22}, {19, 21, 23}, {14 ,22}
	}; //Adjacent play positions of piece[i]
	public static int SIZE_POSSIBLE_MILLS = 16;
	public static int SIZE_MILL = 3;
	public static int SIZE_PIECES = 9;
	public static int SIZE_BOARD = 24;
	
	
	public Thread blackTurnThread;
	public int stepCnt;
	
	private static final Color playerHuman = Color.WHITE, playerAI = Color.BLACK;
	private Piece[] whitePieces = new Piece[SIZE_PIECES];
	private Piece[] blackPieces = new Piece[SIZE_PIECES];
	private Piece[] board = new Piece[SIZE_BOARD];
	
	private Color currentPlayer = playerHuman;
	
	public Board() {
		initBoard();
	}
	public Board(Board original) {
		Piece[] whitePieces = original.getWhitePieces();
		Piece[] blackPieces = original.getBlackPieces();
		Piece[] board = original.getBoard();
		for (int i = 0; i < SIZE_PIECES; i++)
			this.whitePieces[i] = (Piece) whitePieces[i].clone();
		for (int i = 0; i < SIZE_PIECES; i++)
			this.blackPieces[i] = (Piece) blackPieces[i].clone();
		for (int i = 0; i < SIZE_BOARD; i++)
			if (board[i] != null)
				this.board[i] = (Piece) board[i].clone();
	}

	public int getNumberOfPiecesOnBoard(Color player) {
		int count = 0;
		for (Piece piece : this.board) {
			if (piece != null && piece.getPlayer() == player)
				count++;
		}
		return count;
	}
	
	public Piece[] getWhitePieces() {
		return this.whitePieces;
	}
	public Piece[] getBlackPieces() {
		return this.blackPieces;
	}
	public Piece[] getBoard() {
		return this.board;
	}

	public Piece getPieceFromPiecesList(int indexPiece, Color player) {
		if (player == Color.WHITE)
			return whitePieces[indexPiece];
		else
			return blackPieces[indexPiece];
	}
	
	public boolean isMill(Color player, int currentPosition) {
		for (int[] possibleMill : possibleMills) {
			// If one of the positions in evaluated possible mill is empty then skip
			if (board[possibleMill[0]] == null || board[possibleMill[1]] == null || board[possibleMill[2]] == null)
				continue;
			// If current position is not in the evaluated possible mill then skip
			if (!(currentPosition == possibleMill[0] || currentPosition == possibleMill[1] || currentPosition == possibleMill[2]))
				continue;
			// If the evaluated possible mill does not contain pieces of the same color then skip
			if (!(board[possibleMill[0]].getPlayer() == player && board[possibleMill[1]].getPlayer() == player && board[possibleMill[2]].getPlayer() == player))
				continue;
			return true; // Mill was found
		}
		return false; // No mill found
	}
	
	public boolean isAdjacent(int initialPosition, int newPosition) {
		for (int i: possibleSlides[initialPosition]) {
			if (newPosition == i) return true;
		}
		return false;
	}

	public boolean isOccupied(int position) {
		if (board[position] == null)
			return false;
		return true;
	}

	public boolean isActive(int position, Color player) {
		if (player == Color.WHITE && whitePieces[position].active)
			return true;
		if (player == Color.BLACK && blackPieces[position].active)
			return true;
		return false;
	}

	public boolean checkPieceInBoard(int position, Color player) {
		if (player == Color.WHITE && whitePieces[position].indexOnBoard == -1)
			return false;
		if (player == Color.BLACK && blackPieces[position].indexOnBoard == -1)
			return false;
		return true;
	}

	public boolean checkAllPiecesOnBoard(Color player) {
		Piece[] pieces;
		if (player == Color.WHITE)
			pieces = whitePieces;
		else
			pieces = blackPieces;
		for (Piece piece : pieces)
			if (piece.active && piece.indexOnBoard == -1)
				return false;
		return true;
	}

	public void applyMove(Move move, Color player) {
		// Remove piece from old position
		if (move.srcIndexOnBoard >= 0)
			board[move.srcIndexOnBoard] = null;
		// Set piece in new position
		if (player == Color.WHITE) {
			whitePieces[move.indexPiece].indexOnBoard = move.destIndexOnBoard;
			board[move.destIndexOnBoard] = whitePieces[move.indexPiece];
			
			if (move.indexRemovedPieceOnBoard != -1) {
				blackPieces[move.indexRemovedPiece].delete(true, false);
				board[move.indexRemovedPieceOnBoard] = null;
			}
		} else {
			blackPieces[move.indexPiece].indexOnBoard = move.destIndexOnBoard;
			board[move.destIndexOnBoard] = blackPieces[move.indexPiece];
			
			if (move.indexRemovedPieceOnBoard != -1) {
				whitePieces[move.indexRemovedPiece].delete(true, false);
				board[move.indexRemovedPieceOnBoard] = null;
			}
		}
	}
	public void undoMove(Move move, Color player) {
		// Remove piece from new position
		board[move.destIndexOnBoard] = null;
		// Set piece in old position
		if (player == Color.WHITE) {
			whitePieces[move.indexPiece].indexOnBoard = move.srcIndexOnBoard;
			if (move.srcIndexOnBoard >= 0)
				board[move.srcIndexOnBoard] = whitePieces[move.indexPiece];
			// Restore deleted piece
			if (move.indexRemovedPieceOnBoard != -1) {
				blackPieces[move.indexRemovedPiece].restore(move.indexRemovedPieceOnBoard, false);
				board[move.indexRemovedPieceOnBoard] = blackPieces[move.indexRemovedPiece];
			}
		} else {
			blackPieces[move.indexPiece].indexOnBoard = move.srcIndexOnBoard;
			if (move.srcIndexOnBoard >= 0)
				board[move.srcIndexOnBoard] = blackPieces[move.indexPiece];
			// Restore deleted piece
			if (move.indexRemovedPieceOnBoard != -1) {
				whitePieces[move.indexRemovedPiece].restore(move.indexRemovedPieceOnBoard, false);
				board[move.indexRemovedPieceOnBoard] = whitePieces[move.indexRemovedPiece];
			}
		}
	}
	
	public int evaluate(Color player, Color opponent) {
		int score = 0;
		int R1_numPlayerMills = 0, R1_numOppMills = 0;
		int R2_numPlayerTwoPieceConf = 0, R2_numOppTwoPieceConf = 0;

		for(int indexMill = 0; indexMill < SIZE_POSSIBLE_MILLS; indexMill++) {
			int playerPieces = 0, emptyCells = 0, opponentPieces = 0;

			int[] mill = possibleMills[indexMill];
			for(int indexInMill = 0; indexInMill < possibleMills[indexMill].length; indexInMill++) {
				if(board[mill[indexInMill]] == null) {
					emptyCells++;
				} else if(board[mill[indexInMill]].getPlayer() == player) {
					playerPieces++;
				} else {
					opponentPieces++;
				}
			}

			if(playerPieces == 3) {
				R1_numPlayerMills++;
			} else if(playerPieces == 2 && emptyCells == 1) {
				R2_numPlayerTwoPieceConf++;
			} else if(playerPieces == 1 && emptyCells == 2) {
				score += 1;
			} else if(opponentPieces == 3) {
				R1_numOppMills++;
			} else if(opponentPieces == 2 && emptyCells == 1) {
				R2_numOppTwoPieceConf++;
			} else if(opponentPieces == 1 && emptyCells == 2) {
				score += -1;
			}
		}
		
		for (int indexBoard = 0; indexBoard < SIZE_BOARD; indexBoard++) {
			if(indexBoard == 4 || indexBoard == 10 || indexBoard == 13 || indexBoard == 19) {
				if (board[indexBoard] == null)
					score -= 2;
				else if (board[indexBoard].getPlayer() == player)
					score += 2;
			} else if(indexBoard == 1 || indexBoard == 9 || indexBoard == 14 || indexBoard == 22
						|| indexBoard == 7 || indexBoard == 11 || indexBoard == 12 || indexBoard == 16) {
				if (board[indexBoard] == null)
					score -= 1;
				else if (board[indexBoard].getPlayer() == player)
					score += 1;
			}
		}
		
		int coef = 0;
		// number of mills
		switch (Game.currentPhase) {
		case Opening:
			coef = 80;
			break;
		case Middle:
			coef = 120;
			break;
		case Ending:
			coef = 180;
			break;
		default:
			break;
		}
		score += coef*R1_numPlayerMills;
		score -= coef*R1_numOppMills;

		// number of pieces
		switch (Game.currentPhase) {
		case Opening:
			coef = 10;
			break;
		case Middle:
			coef = 8;
			break;
		case Ending:
			coef = 6;
			break;
		default:
			break;
		}
		score += coef*getNumberOfPiecesOnBoard(player);
		score -= coef*getNumberOfPiecesOnBoard(opponent);

		// number of 2 pieces and 1 free spot configuration
		switch (Game.currentPhase) {
		case Opening:
			coef = 12;
			break;
		default:
			coef = 10;
			break;
		}
		score += coef*R2_numPlayerTwoPieceConf;
		score -= coef*R2_numOppTwoPieceConf;
		
		switch (Game.currentPhase) {
		case Opening:
			coef = 10;
			break;
		default:
			coef = 25;
			break;
		}
		
		return score;
	}
	
	@Override
	public boolean movedWhitePiece(Piece movedPiece, int newPosition) {
		// Check valid move
		if (!checkValidWhiteMove(movedPiece.getIndex(), movedPiece.indexOnBoard, newPosition))
			return false;
		// Delete piece at old position
		if (movedPiece.indexOnBoard >= 0) {
			board[movedPiece.indexOnBoard] = null;
		}
		// Add piece at new position
        movedPiece.indexOnBoard = newPosition;
		board[newPosition] = movedPiece;
		// Count turn
		if(Game.currentPhase != GamePhase.Opening)
			stepCnt++;
		// Check for mill
		if (isMill(Color.WHITE, newPosition)) {
			BoardController.millStatus.setVisible(true);
			BoardController.markBlackPiece(this);
			stepCnt = 0;
			return true;
		}
		blackTurn();
		return true;
	}
	
	@Override
	public void removedBlackPiece(int positionOnBoard) {
		// Remove piece from board memory
		board[positionOnBoard].indexOnBoard = -1;
		board[positionOnBoard] = null;
		// Unmark pieces
		BoardController.unmarkBlackPiece(this);
		BoardController.millStatus.setVisible(false);
		blackTurn();
	}
	
	public void endThread() {
		if (blackTurnThread != null) {
			blackTurnThread.interrupt();
			blackTurnThread = null;
		}
	}
	
	private void blackTurn() {
		Game.updateGamePhase(this);
		// If white won, stop the game
		if (Game.currentPhase == Game.GamePhase.Over) {
			Game.gameOver(this);
			return;
		}
		// Else it's black's turn to move
		BoardController.setTurnVisibility(false, true);
		currentPlayer = playerAI;
		blackTurnThread = new Thread(() -> {
			try {
				moveBlackPiece();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
			currentPlayer = playerHuman;
			Game.updateGamePhase(this);
			if (Game.currentPhase == Game.GamePhase.Over) {
				Game.gameOver(this);
			}
			endThread();
		});
		blackTurnThread.start();
	}
	
	private void initBoard() {
		// Initialize black pieces.
		stepCnt = 0;
		int blackIndex = 0;
		for (double centerX = 821.0; centerX < 821.0+14.0*9; centerX += 14.0) {
			blackPieces[blackIndex] = new Piece(Color.BLACK, centerX, 118, Color.WHITESMOKE, blackIndex, this);
			blackIndex++;
		}
		// Initialize white pieces.
		int whiteIndex = 0;
		for (double centerX = 218.0; centerX < 218.0+14.0*9; centerX += 14.0) {
			whitePieces[whiteIndex] = new Piece(Color.WHITE, centerX, 118, Color.BLACK, whiteIndex, this);
			whiteIndex++;
		}
	}
	
	private void moveBlackPiece() throws InterruptedException, ExecutionException {
		// Get next move of AI
		AIPlayer blackPlayer = new AIPlayer(new Board(this));
		Move move = blackPlayer.getBestMove();
		// Create UI task for later execution
		FutureTask<Void> updateUI = new FutureTask<Void>(()->{
			blackPieces[move.indexPiece].setCenterX(BoardController.boardPosition.get(move.destIndexOnBoard).getCenterX());
			blackPieces[move.indexPiece].setCenterY(BoardController.boardPosition.get(move.destIndexOnBoard).getCenterY());
			BoardController.setTurnVisibility(true, false);
		}, null);
		// Move piece
		if (move.srcIndexOnBoard >= 0)
			board[move.srcIndexOnBoard] = null;
		blackPieces[move.indexPiece].indexOnBoard = move.destIndexOnBoard;
		board[move.destIndexOnBoard] = blackPieces[move.indexPiece];
		// Remove piece upon mill
		if (move.indexRemovedPieceOnBoard != -1 && isMill(Color.BLACK, move.destIndexOnBoard)) {
			whitePieces[move.indexRemovedPiece].delete(true, true);
			board[move.indexRemovedPieceOnBoard] = null;
		}
		// Update UI
		Platform.runLater(updateUI);
		updateUI.get();
	}

	private boolean checkValidWhiteMove(int index, int srcIndexOnBoard, int destIndexOnBoard) {
		// If AI is currently playing then skip
		if (currentPlayer == playerAI)
			return false;
		// If there is a mill and the program is waiting for the user to delete a black piece then skip
		if (BoardController.millStatus.isVisible())
			return false;
		// If the closest position already holds a piece then skip
    	if (isOccupied(destIndexOnBoard))
    		return false;
    	// Check move condition depending on game phase
    	switch (Game.currentPhase) {
    	case Opening:
    		// If the moved piece is already on the board then skip
    		if (checkPieceInBoard(index, Color.WHITE))
    			return false;
    		break;
    	case Middle:
    		// If the destination position is not adjacent to the current position then skip
    		if (!isAdjacent(srcIndexOnBoard, destIndexOnBoard))
    			return false;
    		break;
    	case Ending:
    		// If the destination position is not adjacent to the current position and there are more than 3 white pieces left then skip
    		if (!isAdjacent(srcIndexOnBoard, destIndexOnBoard) && getNumberOfPiecesOnBoard(Color.WHITE) > Game.MIN_PIECES_ON_BOARD)
    			return false;
    		break;
		default:
			return false;
    	}
    	// Valid move
    	return true;
	}
}