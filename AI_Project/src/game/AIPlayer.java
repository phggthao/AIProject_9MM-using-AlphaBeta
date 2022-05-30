package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import board.Board;
import board.BoardController;
import board.Piece;
import javafx.scene.paint.Color;

public class AIPlayer {
	public static final int MAX_PIECES_ON_BOARD = 9;
	public static final int MIN_PIECES_ON_BOARD = 3;

	private static final Color playerHuman = Color.WHITE, playerAI = Color.BLACK;
	private static final Random rand = new Random();
	private int selectedDepth;
	private Board board;
	
	public AIPlayer(Board board) {
		this.board = board;
		this.selectedDepth = BoardController.getAlgorithmDepth();
	}
	
	public Move getBestMove() {
		ArrayList<Move> moves = getPossibleMoves(playerAI, playerHuman);

		for(Move move : moves) {
			board.applyMove(move, playerAI);
			move.score += alphaBeta(selectedDepth - 1, Integer.MIN_VALUE, Integer.MAX_VALUE, playerHuman, playerAI);
			board.undoMove(move, playerAI);
		}
		Collections.sort(moves, new HeuristicComparator());

		// if there are different moves with the same score it returns one of them randomly
		ArrayList<Move> bestMoves = new ArrayList<Move>();
		int bestScore = moves.get(0).score;
		bestMoves.add(moves.get(0));
		for(int i = 1; i < moves.size(); i++) {
			if(moves.get(i).score == bestScore) {
				bestMoves.add(moves.get(i));
			} else {
				break;
			}
		}
		return bestMoves.get(rand.nextInt(bestMoves.size()));
	}

	private int alphaBeta(int depth, int alpha, int beta, Color player, Color opponent) {
		ArrayList<Move> possibleMoves;
		
		if (depth == 0) {
			return board.evaluate(player, opponent);
		} else if ((possibleMoves = getPossibleMoves(player, opponent)).isEmpty()) {
			if(player == playerAI) {
				return Integer.MIN_VALUE;
			} else {
				return Integer.MAX_VALUE;
			}
		} else {
			for (Move move : possibleMoves) {
				board.applyMove(move, player);
				if (player == playerAI) {  // maximizing player
					alpha = Math.max(alpha, alphaBeta(depth - 1, alpha, beta, opponent, player));
					if (beta <= alpha) {
						board.undoMove(move, player);
						break; // cutoff
					}
				} else {  //  minimizing player
					int eval = alphaBeta(depth - 1, alpha, beta, opponent, player);
					beta = Math.min(beta, eval);
					if (beta <= alpha) {
						board.undoMove(move, player);
						break; // cutoff
					}
				}
				board.undoMove(move, player);
			}

			if(player == playerAI) {
				return alpha;
			} else {
				return beta;
			}
		}
	}
	
	private ArrayList<Move> getPossibleMoves(Color player, Color opponent) {
		// Get all of the possible next moves
		ArrayList<Move> possibleMoves = new ArrayList<Move>();
		Piece[] board = this.board.getBoard();
		
		switch (Game.currentPhase) {
		case Opening:
			for (int indexPiece = 0; indexPiece < Board.SIZE_PIECES; indexPiece++) {
				// If piece is not playable then skip
				if (!this.board.isActive(indexPiece, player))
					continue;
				// If piece is already on the board then skip
				if (this.board.checkPieceInBoard(indexPiece, player))
					continue;
				// Create move
				Piece piece = this.board.getPieceFromPiecesList(indexPiece, player);
				for (int indexBoard = 0; indexBoard < Board.SIZE_BOARD; indexBoard++) {
					if (!this.board.isOccupied(indexBoard)) {
						board[indexBoard] = piece;
						possibleMoves.addAll(checkMove(player, opponent, new Move(indexPiece, piece.indexOnBoard, indexBoard, -1, -1)));
						board[indexBoard] = null;
					}
				}
			}
			break;
		case Middle:
			for(int indexBoard = 0; indexBoard < Board.SIZE_BOARD; indexBoard++) {
				if(board[indexBoard] != null && board[indexBoard].getPlayer() == player) { // for each piece of the player
					Piece piece = board[indexBoard];
					int[] adjacent = Board.possibleSlides[indexBoard];
					// check valid moves to adjacent positions
					for(int indexInAdjacent = 0; indexInAdjacent < adjacent.length; indexInAdjacent++) {
						if (!this.board.isOccupied(adjacent[indexInAdjacent])) {
							board[adjacent[indexInAdjacent]] = piece;
							possibleMoves.addAll(checkMove(player, opponent, new Move(piece.getIndex(), indexBoard, adjacent[indexInAdjacent], -1, -1)));
							board[adjacent[indexInAdjacent]] = null;
						}
					}
				}
			}
			break;
		case Ending:
			ArrayList<Integer> freeSpaces = new ArrayList<Integer>();
			ArrayList<Integer> playerSpaces = new ArrayList<Integer>();

			for(int indexBoard = 0; indexBoard < Board.SIZE_BOARD; indexBoard++) {
				if(board[indexBoard] != null && board[indexBoard].getPlayer() == player) {
					playerSpaces.add(indexBoard);
				} else if (board[indexBoard] == null) {
					freeSpaces.add(indexBoard);
				}
			}

			// for every piece the player has on the board
			for(int indexPlayerBoard = 0; indexPlayerBoard < playerSpaces.size(); indexPlayerBoard++) {
				Piece piece = board[playerSpaces.get(indexPlayerBoard)];
				board[playerSpaces.get(indexPlayerBoard)] = null;
				// each empty space is a valid move
				for(int indexFreeBoard = 0; indexFreeBoard < freeSpaces.size(); indexFreeBoard++) {
					board[freeSpaces.get(indexFreeBoard)] = piece;
					possibleMoves.addAll(checkMove(player, opponent, new Move(piece.getIndex(), piece.indexOnBoard, freeSpaces.get(indexFreeBoard), -1, -1)));
					board[freeSpaces.get(indexFreeBoard)] = null;
				}
				board[playerSpaces.get(indexPlayerBoard)] = piece;
			}
			break;
		default:
			break;
		}
		return possibleMoves;
	}

	private ArrayList<Move> checkMove(Color player, Color opponent, Move move) {
		ArrayList<Move> moves = new ArrayList<Move>();
		boolean madeMill = false;
		Piece[] board = this.board.getBoard();

		for (int[] row : Board.possibleMills) {
			if (this.board.isMill(player, row[0])) { // made a mill - select piece to remove
				madeMill = true;

				boolean erased = false;
				// Try to erase a non-mill piece
				for (int indexBoard = 0; indexBoard < Board.SIZE_BOARD; indexBoard++) {
					if ((!this.board.isMill(opponent, indexBoard)) && (board[indexBoard] != null) && (board[indexBoard].getPlayer() == opponent)) {
						move.indexRemovedPiece = board[indexBoard].getIndex();
						move.indexRemovedPieceOnBoard = indexBoard;
						// add a move for each piece that can be removed, this way it will check what's the best one to remove
						moves.add(new Move(move));
						erased = true;
					}
				}
				// If opponent only has mills on board then erase any of his pieces
				if (!erased) {
					for (int indexBoard = 0; indexBoard < Board.SIZE_BOARD; indexBoard++) {
						if ((board[indexBoard] != null) && (board[indexBoard].getPlayer() == opponent)) {
							move.indexRemovedPiece = board[indexBoard].getIndex();
							move.indexRemovedPieceOnBoard = indexBoard;
							// add a move for each piece that can be removed, this way it will check what's the best one to remove
							moves.add(new Move(move));
						}
					}
				}
			}
		}

		if(!madeMill) // don't add repeated moves
			moves.add(move);
		return moves;
	}
	
	private class HeuristicComparator implements Comparator<Move> {
		@Override
		public int compare(Move t, Move t1) {
			return t1.score - t.score;
		}
	}
}