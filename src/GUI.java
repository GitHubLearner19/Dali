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
import javafx.geometry.Insets;
import javafx.stage.StageStyle;

public class GUI extends Application {
   @Override
   public void start(Stage primaryStage) {
      GridPane rootPane = new GridPane();
      
      rootPane.setPadding(new Insets(50, 50, 50, 50));
      rootPane.setHgap(50);
      
      Board board = new Board(rootPane);
      SideBar sidebar = new SideBar(board);
      board.setSideBar(sidebar);
      
      rootPane.setAlignment(Pos.CENTER);
      
      board.update();
      board.display();
      
      sidebar.display(rootPane);
      
      Scene scene = new Scene(rootPane, 800, 550);
      primaryStage.setTitle("Dali Chess");
      primaryStage.setScene(scene);
      primaryStage.show();
      primaryStage.setMinWidth(primaryStage.getWidth());
      primaryStage.setMinHeight(primaryStage.getHeight());
   }
}




