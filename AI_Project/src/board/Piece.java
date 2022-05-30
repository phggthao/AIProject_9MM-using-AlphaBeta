package board;

import game.Game;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

public class Piece extends Circle implements Cloneable {
    boolean marked = false; // Piece is marked for deletion
    
    public int indexOnBoard = -1; // Current position on game board [0,23]
    public boolean active = true; //Check if that piece is still available to play
    
    private Board board;
    private double initialX, initialY;
    private int index; // Index when generate pieces, stay constant for entire game
    
    public Piece(Color player, double x, double y, Color stroke, int index, Board board) {
		this.setFill(player);
		this.setCenterX(x);
		this.setCenterY(y);
		this.setRadius(14.0);
		this.setStroke(stroke);
		this.setStrokeType(StrokeType.INSIDE);
		this.setStrokeWidth(0.3);
		
		this.initialX = x;
		this.initialY = y;
		this.index = index;
		this.board = board;
		
		this.setOnMousePressed(evt -> handleMousePress(evt));
		if (player == Color.WHITE) {
			this.setOnMouseDragged(evt -> handleMouseDrag(evt));
			this.setOnMouseReleased(evt -> handleMouseRelease(evt));
		}
    }
    
    public Piece(Color player, int index) {
		this.setFill(player);
		this.indexOnBoard = index;
    }
    
    @Override
	public Object clone() {
    	Piece piece = this;
	    try {
	    	piece = (Piece) super.clone();
	    } catch (CloneNotSupportedException e) { }
	    return piece;
	}
    
    public int getIndex() {
    	return this.index;
    }
    
    public Color getPlayer() {
    	return (Color) this.getFill();
    }
    
    public void delete(boolean updateIndex, boolean updateUI) {
    	this.active = false;
    	this.marked = false;
		if (updateIndex)
			this.indexOnBoard = -1;
		if (updateUI)
			this.setVisible(false);
    }
    public void restore(int index, boolean updateUI) {
    	this.active = true;
		this.indexOnBoard = index;
		if (updateUI)
			this.setVisible(true);
    }
    
	// Mouse interactions
    private void handleMousePress(MouseEvent evt){
		if ((Color) this.getFill() == Color.WHITE) {
			this.setFill(Color.YELLOW);
		} else {
	    	if (this.marked) {
	    		delete(false, true);
                this.board.removedBlackPiece(this.indexOnBoard);
	    	}
		}
    }
	
    private void handleMouseDrag(MouseEvent evt){
    	this.setCenterX(evt.getX());
    	this.setCenterY(evt.getY());
    	
    	switch(Game.currentPhase) {
    	case Opening:
    		for(int i=0;i<24;i++) 
    			BoardController.boardPosition.get(i).setFill(Color.rgb(84, 255, 135));
    		break;
    	case Middle:
    		for (int i=0; i<24; i++) {
    			if ((!board.isOccupied(i) && board.isAdjacent(this.indexOnBoard,i)) || i == this.indexOnBoard)
    				BoardController.boardPosition.get(i).setFill(Color.rgb(84, 255, 135));
    		}
    		break;
    	case Ending:
    		for(int i=0;i<24;i++) {
    			if (board.getNumberOfPiecesOnBoard(Color.WHITE) < Game.MIN_PIECES_ON_BOARD)
    				BoardController.boardPosition.get(i).setFill(Color.rgb(84, 255, 135));
    			else if ((!board.isOccupied(i) && board.isAdjacent(this.indexOnBoard,i)) || i == this.indexOnBoard)
        				BoardController.boardPosition.get(i).setFill(Color.rgb(84, 255, 135));
    		}
    		break;
		default:
			break;
    	}
    }
    
    private void handleMouseRelease(MouseEvent evt){
    	this.setFill(Color.WHITE);
        double releaseX = evt.getSceneX();
        double releaseY = evt.getSceneY();
        for(int i=0; i<BoardController.boardPosition.size();i++){
            double tempX = BoardController.boardPosition.get(i).getCenterX();
            double tempY = BoardController.boardPosition.get(i).getCenterY();
            // If the piece was release close enough to a valid position then snap it to that position
            if(releaseX >= tempX-50 && releaseX <= tempX+50 && releaseY >= tempY-50 && releaseY <= tempY+50) {
                // Notify listeners that a white piece has been moved
                if (!this.board.movedWhitePiece(this, i))
                	continue;
                for(int j=0;j<BoardController.boardPosition.size();j++) BoardController.boardPosition.get(j).setFill(Color.TRANSPARENT);
                // Set new piece origin
                this.initialX = tempX;
                this.initialY = tempY;
                break;
            }
            // If the position of the piece doesn't change after drag
            else for(int j=0;j<BoardController.boardPosition.size();j++) BoardController.boardPosition.get(j).setFill(Color.TRANSPARENT);
        }
    	this.setCenterX(initialX);
    	this.setCenterY(initialY);
    }
}