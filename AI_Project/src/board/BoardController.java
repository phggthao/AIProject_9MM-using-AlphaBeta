package board;

import javafx.fxml.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.collections.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class BoardController implements Initializable{
	public static ArrayList<Circle> boardPosition = new ArrayList<Circle>();
    public static Text millStatus, gameResult;
    public static ArrayList<Group> crossPosition = new ArrayList<Group>();
    public static Button s_restartButton;
    
    private static Text s_whiteWin, s_blackWin, s_draw;
    private static Group s_whiteTurn, s_blackTurn;
    private static ProgressBar s_blackProgress;
    private static ChoiceBox<String> s_algoChoiceBox;
    
    @FXML
    private ChoiceBox<String> algoChoiceBox;
    @FXML
    private Circle pos1,pos2,pos3,pos4,pos5,pos6,pos7,pos8,pos9,pos10,pos11,pos12,pos13,pos14,pos15,pos16,pos17,pos18,pos19,pos20,pos21,pos22,pos23,pos24;
    @FXML
    private Pane pane;
    @FXML
    private Group whiteTurn, blackTurn;
    @FXML
    private Group x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22,x23,x24; // crosses to mark erasable pieces
    @FXML
    private ProgressBar blackProgress;
    @FXML
    private Button restart;
    
    @Override
    public void initialize(URL arg0, ResourceBundle arg1) {
    	ArrayList<String> st = new ArrayList<>();
        st.add("Alpha-Beta, 2 moves ahead");
        st.add("Alpha-Beta, 4 moves ahead");
        st.add("Alpha-Beta, 6 moves ahead");
        st.add("Alpha-Beta, 8 moves ahead");
        st.add("Alpha-Beta, 10 moves ahead");
        algoChoiceBox.setItems(FXCollections.observableArrayList(st));
        algoChoiceBox.getSelectionModel().selectFirst();
        s_algoChoiceBox = algoChoiceBox;
        
        Collections.addAll(boardPosition,pos1,pos2,pos3,pos4,pos5,pos6,pos7,pos8,pos9,pos10,pos11,pos12,pos13,pos14,pos15,pos16,pos17,pos18,pos19,pos20,pos21,pos22,pos23,pos24);
        Collections.addAll(crossPosition,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x20,x21,x22,x23,x24);
        for (int i=0; i<24; i++) {
        	crossPosition.get(i).setVisible(false);
        }
        
        s_whiteTurn = whiteTurn;
        s_blackTurn = blackTurn;
        s_blackProgress = blackProgress; 
        s_restartButton = restart;
        
        millStatus = new Text(550, 466, "Mill!");
        millStatus.setFont(Font.font("Georgia", FontWeight.BOLD, 30));
        millStatus.setVisible(false);
        pane.getChildren().add(millStatus);
        
        s_draw = new Text(524, 470, "DRAW");
        s_draw.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        s_blackWin = new Text(522, 448, "BLACK\n  WIN!");
        s_blackWin.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
        s_whiteWin = new Text(516, 448, "WHITE\n   WIN!");
        s_whiteWin.setFont(Font.font("Georgia", FontWeight.BOLD, 36));
		BoardController.setGameResultVisibility(false, false, false);
        pane.getChildren().addAll(s_draw, s_whiteWin, s_blackWin);
    }
    
	public static void markBlackPiece(Board board) {	// Mark erasable pieces
		Piece[] gameBoard = board.getBoard();
		boolean marked = false;
		for (int i=0; i<24; i++) {
			if (board.isOccupied(i) && gameBoard[i].getPlayer() == Color.BLACK && !board.isMill(Color.BLACK, i)) {
				BoardController.crossPosition.get(i).setVisible(true);
				BoardController.crossPosition.get(i).toFront();
				gameBoard[i].marked = true;
				marked = true;
			}
		}
		if (marked)
			return;
		for (int i=0; i<24; i++) {
			if (board.isOccupied(i) && gameBoard[i].getPlayer() == Color.BLACK) {
				BoardController.crossPosition.get(i).setVisible(true);
				BoardController.crossPosition.get(i).toFront();
				gameBoard[i].marked = true;
			}
		}
	}
	public static void unmarkBlackPiece(Board board) {	// Unmark the pieces after delete
		Piece[] gameBoard = board.getBoard();
		for (int i=0; i<24; i++) {
			BoardController.crossPosition.get(i).setVisible(false);
			if (gameBoard[i] != null && gameBoard[i].getPlayer() == Color.BLACK)
				gameBoard[i].marked = false;
		}
	}
	
	public static void bringPiecesToFront(Board board, Color color) {
		Piece[] gameBoard = board.getBoard();
		for (int i=0; i<24; i++) {
			if (board.isOccupied(i) && gameBoard[i].getPlayer() == color) 
				gameBoard[i].toFront();
		}
	}

	public static void setTurnVisibility(boolean white, boolean black) {
		s_whiteTurn.setVisible(white);
		s_blackTurn.setVisible(black);
		s_blackProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        if (black) {
            s_blackProgress.setStyle("-fx-accent: black;");
        }
        else {
            s_blackProgress.setStyle("-fx-accent: transparent;");
        }
	}
	
	public static void setGameResultVisibility(boolean draw, boolean white, boolean black) {
		millStatus.setVisible(false);
		s_draw.setVisible(draw);
		s_whiteWin.setVisible(white);
		s_blackWin.setVisible(black);
	}
	
	public static int getAlgorithmDepth() {
		switch(s_algoChoiceBox.getSelectionModel().getSelectedIndex()) {
		case 1:
			return 4;
		case 2:
			return 6;
		case 3:
			return 8;
		case 4:
			return 10;
		default:
			return 2;
		}
	}
}