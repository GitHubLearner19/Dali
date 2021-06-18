/**
 *
 *
 *
 */

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.stage.StageStyle;

public class GUI extends Application {
   @Override
   public void start(Stage primaryStage) {
      Board board = new Board();
      
      GridPane rootPane = new GridPane();
      
      rootPane.setAlignment(Pos.CENTER);
      
      board.update();
      board.display(rootPane);
      
      Scene scene = new Scene(rootPane, 800, 550);
      primaryStage.setTitle("Dali Chess");
      primaryStage.setScene(scene);
      primaryStage.show();
      primaryStage.setMinWidth(primaryStage.getWidth());
      primaryStage.setMinHeight(primaryStage.getHeight());
   }
}




