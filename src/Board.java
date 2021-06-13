/**
 *
 *
 *
 */

import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.text.*;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.Node;
import javafx.geometry.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;

public class Board {
   private BorderPane pane = new BorderPane();
   
   private StackPane stack = new StackPane();
   
   // grid of squares
   private GridPane grid = new GridPane();
   
   // squares
   private Square[] SQUARES = new Square[64];
   
   private Square startSquare;
   private Square targetSquare;
   
   private boolean moving = false;
   private boolean dragging = false;
   
   // pieces
   private ArrayList<Piece> pieces = new ArrayList<>();
   
   // outside rectangles
   private Rectangle r1 = new Rectangle(25, 400);
   private Rectangle r2 = new Rectangle(450, 25);
   private Rectangle r3 = new Rectangle(25, 400);
   private Rectangle r4 = new Rectangle(450, 25);
   
   // outside file/rank text
   private VBox box1 = new VBox();
   private HBox box2 = new HBox();
   private VBox box3 = new VBox();
   private HBox box4 = new HBox();
   
   public Board() {
      for (int i = 0; i < 8; i ++) {
         for (int j = 0; j < 8; j ++) {
            SQUARES[i * 8 + j] = new Square(j, 7 - i);
         }
      }
   }
   
   /* Square Class */
   
   class Square {
      private int column = 0;
      private int row = 0;
      
      private Rectangle r = new Rectangle(50, 50);
      
      private Square() {
         r.setFill(Color.BURLYWOOD);
      }
      
      private Square(int column, int row) {
         this.column = column;
         this.row = row;
         r.setFill((row + column) % 2 == 0 ? Color.BURLYWOOD : Color.SIENNA);
      }
      
      public int getColumn() {
         return column;
      }
      
      public int getRow() {
         return row;
      }
      
      public void click() {
         r.setOnMouseClicked(
            e -> {
               startSquare = this;
               r.setFill(Color.YELLOW);
            });
      }
      
      public void drag() {
         r.setOnMouseDragged(
            e -> {
               startSquare = this;
               r.setFill(Color.YELLOW);
               dragging = true;
            });
      }
      
      public void press() {
         r.setOnMousePressed(
            e -> {
               targetSquare = this;
            });
      }
      
      public void release() {
         r.setOnMouseReleased(
            e -> {
               targetSquare = this;
            });
      }
      
      public Rectangle get() {
         return r;
      }
      
      public void display() {
         grid.add(r, column, row);
      }
   }
   
   public void update() {
      grid.addEventFilter(MouseEvent.MOUSE_CLICKED, 
         e -> {
            if (moving || startSquare != null) {
               e.consume();
            }
         }); // mouse click
      
      grid.addEventFilter(MouseEvent.MOUSE_DRAGGED, 
         e -> {
            if (moving || startSquare != null) {
               e.consume();
            }
         }); // mouse drag
      
      grid.addEventFilter(MouseEvent.MOUSE_PRESSED, 
         e -> {
            if (moving || startSquare == null) {
               e.consume();
            } else if (dragging) {
               // set piece position
               e.consume();
            }
         }); // mouse pressed
      
      grid.addEventFilter(MouseEvent.MOUSE_RELEASED, 
         e -> {
            if (moving || startSquare == null) {
               e.consume();
            }
         }); // mouse released
      
      for (int i = 0; i < 64; i ++) {
         SQUARES[i].click();
         SQUARES[i].drag();
         SQUARES[i].press();
         SQUARES[i].release();
      }
   }
   
   private void outline() {
      
      // left
      
      StackPane p1 = new StackPane();
      
      StackPane.setAlignment(box1, Pos.CENTER);
      
      r1.setFill(Color.SADDLEBROWN);
      
      box1.setSpacing(30);
      box1.setAlignment(Pos.CENTER);
      
      for (int i = 8; i > 0; i --) {
         Label l = new Label("" + i);
         l.setFont(Font.font("Times New Roman", 18));
         
         box1.getChildren().add(l);
      }
      
      p1.getChildren().addAll(r1, box1);
      
      // right
      
      StackPane p3 = new StackPane();
      
      StackPane.setAlignment(box3, Pos.CENTER);
      
      r3.setFill(Color.SADDLEBROWN);
      
      box3.setSpacing(30);
      box3.setAlignment(Pos.CENTER);
      
      for (int i = 8; i > 0; i --) {
         Label l = new Label("" + i);
         l.setFont(Font.font("Times New Roman", 18));
         l.setRotate(180);
         
         box3.getChildren().add(l);
      }
      
      p3.getChildren().addAll(r3, box3);
      
      // top
      
      StackPane p2 = new StackPane();
      
      StackPane.setAlignment(box2, Pos.CENTER);
      
      r2.setFill(Color.SADDLEBROWN);
      
      box2.setSpacing(41);
      box2.setAlignment(Pos.CENTER);
      
      for (int i = 0; i < 8; i ++) {
         Label l = new Label(String.valueOf((char) (97 + i)));
         l.setFont(Font.font("Times New Roman", 20));
         l.setRotate(180);
         
         box2.getChildren().add(l);
      }
      
      p2.getChildren().addAll(r2, box2);
      
      // bottom
      
      StackPane p4 = new StackPane();
      
      StackPane.setAlignment(box4, Pos.CENTER);
      
      r4.setFill(Color.SADDLEBROWN);
      
      box4.setSpacing(41);
      box4.setAlignment(Pos.CENTER);
      
      for (int i = 0; i < 8; i ++) {
         Label l = new Label(String.valueOf((char) (97 + i)));
         l.setFont(Font.font("Times New Roman", 20));
         
         box4.getChildren().add(l);
      }
      
      p4.getChildren().addAll(r4, box4);
      
      // display on pane
      pane.setLeft(p1);
      pane.setRight(p3);
      pane.setTop(p2);
      pane.setBottom(p4);
   }
   
   public void display(GridPane rootPane) {
      outline();
      
      for (int i = 0; i < 64; i ++) {
         SQUARES[i].display();
      }
      
      Pane stack = new Pane();
      
      stack.getChildren().add(grid);
      
      pane.setCenter(stack);
      
      rootPane.add(pane, 0, 0);
   }
   
   public void flip() {
      pane.setRotate(180);
   }
   
   public void resize(GridPane rootPane) {
      
   }
}




