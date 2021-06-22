/**
 *
 *
 *
 */

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.text.Font;

public class SideBar {
   private VBox stack = new VBox();
   private Board board;
   private TextArea notation = new TextArea();
   private RadioButton pgn = new RadioButton("PGN");
   private RadioButton fen = new RadioButton("FEN");
   private boolean undoLock = true;
   
   public SideBar(Board board) {
      this.board = board;
   }
   
   public void update() {
      if (pgn.isSelected()) {
         notation.setText(board.getState().toPGN());
      } else {
         notation.setText(board.getState().toFEN());
      }
   }
   
   public void display(GridPane root) {
      stack.setAlignment(Pos.TOP_CENTER);
      stack.setSpacing(20);
      
      notation.setEditable(false);
      notation.setWrapText(true);
      notation.setPrefColumnCount(15);
      notation.setFont(Font.font("Times New Roman", 20));
      stack.getChildren().add(notation);
      
      ScrollPane scrollPane = new ScrollPane(notation);
      stack.getChildren().add(scrollPane);
      
      ToggleGroup group = new ToggleGroup();
      pgn.setFont(Font.font("Times New Roman", 20));
      fen.setFont(Font.font("Times New Roman", 20));
      pgn.setToggleGroup(group);
      fen.setToggleGroup(group);
      pgn.setSelected(true);
      
      HBox radio = new HBox();
      
      radio.setAlignment(Pos.CENTER);
      radio.setSpacing(30);
      radio.getChildren().addAll(pgn, fen);
      
      stack.getChildren().add(radio);
      
      pgn.setOnAction(e -> {
         notation.setText(board.getState().toPGN());
      });
      
      fen.setOnAction(e -> {
        notation.setText(board.getState().toFEN());
      });
      
      // flip button
      
      Button flip = new Button("Flip");
      flip.setFont(Font.font("Times New Roman", 20));
      
      Button undobtn = new Button("Undo");
      undobtn.setFont(Font.font("Times New Roman", 20));
      
      stack.getChildren().addAll(flip, undobtn);
      
      flip.setOnAction(e -> {
         board.flip();
      });
      
      undobtn.setOnAction(e -> {
         if (!board.isMoving()) {
            board.undo();
            update();
         }
      });
      
      root.add(stack, 1, 0);
   }
}




