package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import board.*;
import game.Game;

public class Main extends Application{
	private static Scene scene = null;
	Board board;

	public void start(Stage primaryStage) throws Exception {
		Pane root = FXMLLoader.load(getClass().getResource("/board/Board.fxml"));
		scene = new Scene(root, 1200, 800);
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Nine Men's Morris");
		primaryStage.setResizable(false);
		primaryStage.getIcons().add(new Image("/images/icon.png"));
		primaryStage.show();
		
		board = new Board();

		// Data bind game pieces
		Pane pane = (Pane) scene.lookup("#pane");
		pane.getChildren().addAll(board.getBlackPieces());
		pane.getChildren().addAll(board.getWhitePieces());
		
		BoardController.s_restartButton.setOnAction(evt -> {
			// Remove current game
			pane.getChildren().removeAll(board.getBlackPieces());
			pane.getChildren().removeAll(board.getWhitePieces());
			// Kill child thread
			board.endThread();
			// Initialize new game board
			board = new Board();
			pane.getChildren().addAll(board.getBlackPieces());
			pane.getChildren().addAll(board.getWhitePieces());
			// Set turn to user
			BoardController.setTurnVisibility(true, false);
			BoardController.setGameResultVisibility(false, false, false);
			BoardController.millStatus.setVisible(false);
			for (int i=0; i<24; i++) {
				BoardController.crossPosition.get(i).setVisible(false);
			}
			// Reset phase
			Game.currentPhase = Game.GamePhase.Opening;
		});
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}