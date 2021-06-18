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
import javafx.animation.PathTransition;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar.ButtonData;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

public class Board {
   private BorderPane pane = new BorderPane();
   
   private Chess state = new Chess();
   private Move[] moves = state.moves();
   
   // players
   private Map<Character,String> players = new HashMap<>(); 
   
   // grid of squares
   private GridPane grid = new GridPane();
   
   private Pane stack = new Pane();
   
   private double cellSize = 50;
   
   // squares
   private Square[] SQUARES = new Square[64];
   
   private Square startSquare;
   private Square targetSquare;
   
   private boolean moving = false;
   
   // pieces
   private ArrayList<Piece> pieces = new ArrayList<>();
   
   private Piece selected; // selected piece
   
   // dialog box
   private Dialog<String> dialog = new Dialog<>();
   
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
            SQUARES[i * 8 + j] = new Square(j, 7 - i, i * 8 + j);
            
            char piece = state.toString().charAt(i * 9 + j);
            
            if (piece != '·') {
               pieces.add(new Piece(piece, j, i));
            }
         }
      }
      
      grid.setHgap(0);
      grid.setVgap(0);
      
      players.put('w', "user");
      players.put('b', "user");
      
      ButtonType queen = new ButtonType("Queen", ButtonData.OK_DONE);
      ButtonType rook = new ButtonType("Rook", ButtonData.OK_DONE);
      ButtonType bishop = new ButtonType("Bishop", ButtonData.OK_DONE);
      ButtonType knight = new ButtonType("Knight", ButtonData.OK_DONE);
      
      dialog.getDialogPane().getButtonTypes().addAll(queen, rook, bishop, knight);
      dialog.getDialogPane().setStyle("-fx-font-family: Times New Roman");
      dialog.setResultConverter(
         bt -> {
            if (bt.getText() == "Knight") {
               return "N";
            } else {
               return bt.getText();
            }
         });
   }
   
   /* Square Class */
   
   class Square {
      private int column = 0;
      private int row = 0;
      private int index = 0;
      private Color color;
      
      private Rectangle r = new Rectangle(cellSize, cellSize);
      
      private Square() {
         color = Color.BURLYWOOD;
         fill();
      }
      
      private Square(int column, int row, int index) {
         this.column = column;
         this.row = row;
         this.index = index;
         color = (row + column) % 2 == 0 ? Color.BURLYWOOD : Color.SIENNA;
         fill();
      }
      
      public void fill() {
         r.setFill(color);
      }
      
      public int getColumn() {
         return column;
      }
      
      public int getRow() {
         return row;
      }
      
      public int getIndex() {
         return index;
      }
      
      public void click() {
         r.setOnMouseClicked(
            e -> {
               selected = getPiece(this);
               
               if (selected != null) {
                  startSquare = this;
                  r.setFill(Color.YELLOW);
               }
            });
      }
      
      public void press() {
         r.setOnMousePressed(
            e -> {
               if (this != startSquare) {
                  targetSquare = this;
                  startSquare.fill();
                     
                  Move[] m = getMoves();
                     
                  if (m.length > 0 && players.get(state.getTurn()) == "user") {
                     move(m);
                  } else {
                     startSquare = null;
                     targetSquare = null;
                     selected = null;
                  }
               }
            });
      }
      
      public void drag() {
         r.setOnMouseDragged(
            e -> {
               selected = getPiece(this);
               
               if (selected != null) {
                  startSquare = this;
                  r.setFill(Color.YELLOW);
               }
            });
      }
      
      public Rectangle get() {
         return r;
      }
      
      public void display() {
         grid.add(r, column, row);
      }
   }
   
   private Piece getPiece(Square s) {
      for (Piece piece : pieces) {
         if (piece.getColumn() == s.getColumn() && piece.getRow() == s.getRow()) {
            return piece;
         }
      }
      
      return null;
   }
   
   private Move[] getMoves() {
      ArrayList<Move> list = new ArrayList<>();
      
      for (Move m : moves) {
         if (m.getStart() == startSquare.getIndex() && m.getTarget() == targetSquare.getIndex()) {
            list.add(m);
         }
      }
      
      return list.toArray(new Move[0]);
   }
   
   public void move(Move[] m) {
      
      moving = true;
   
      // handle capture
      
      Piece capturePiece;
      
      if (m[0].getEnPassant()) {
         capturePiece = getPiece(SQUARES[8 * (7 - startSquare.getRow()) + targetSquare.getColumn()]);
      } else {
         capturePiece = getPiece(targetSquare);
      }
      
      if (capturePiece != null) {
         capturePiece.fade(
            e -> {
               stack.getChildren().remove(capturePiece.get());
               pieces.remove(capturePiece);
            });
      }
      
      // move
   
      selected.move(targetSquare.getColumn(), targetSquare.getRow(), 
         e -> {
            if (m.length > 1) {
               dialog.show();
               dialog.setOnHidden(
                  evt -> {
                     for (int i = 0; i < 4; i ++) {
                        if (Character.toUpperCase(m[i].getPromote()) == dialog.getResult().charAt(0)) {
                           state = state.move(m[i]);
                           moves = state.moves();
                           selected.set(m[i].getPromote());
                           startSquare = null;
                           targetSquare = null;
                           moving = false;
                           selected = null;
                           break;
                        }
                     }
                  });
            } else {
               state = state.move(m[0]);
               moves = state.moves();
               startSquare = null;
               targetSquare = null;
               moving = false;
               selected = null;
            }
         });
         
      // castling
      
      switch (m[0].getCastle()) {
         case 'K':
            getPiece(SQUARES[7]).move(5, 7, e -> {});
            break;
         case 'Q':
            getPiece(SQUARES[0]).move(3, 7, e -> {});
            break;
         case 'k':
            getPiece(SQUARES[63]).move(5, 0, e -> {});
            break;
         case 'q':
            getPiece(SQUARES[56]).move(3, 0, e -> {});
      }
   }
   
   public void update() {
      grid.addEventFilter(MouseEvent.MOUSE_CLICKED, 
         e -> {
            if (moving || players.get(state.getTurn()) != "user") {
               e.consume();
            }
         }); // mouse click
         
      grid.addEventFilter(MouseEvent.MOUSE_DRAGGED, 
         e -> {
            if (moving || players.get(state.getTurn()) != "user") {
               e.consume();
            } else if (startSquare != null) {
               selected.setX(e.getX());
               selected.setY(e.getY());
               e.consume();
            }
         });
         
      grid.addEventFilter(MouseEvent.MOUSE_PRESSED, 
         e -> {
            if (moving || startSquare == null) {
               e.consume();
            }
         });
      
      grid.setOnMouseReleased(
         e -> {
            if (!moving && startSquare != null) {
               Square square = new Square();
               
               for (Square s : SQUARES) {
                  int c = (int) (e.getX() / cellSize);
                  int r = (int) (e.getY() / cellSize);
                  
                  if (s.getColumn() == c && s.getRow() == r) {
                     square = s;
                     break;
                  }
               }
               
               targetSquare = square;
               startSquare.fill();
                  
               Move[] m = getMoves();
                  
               if (m.length > 0 && players.get(state.getTurn()) == "user") {
                  move(m);
               } else {
                  selected.move(startSquare.getColumn(), startSquare.getRow(), evt -> {});
                  startSquare = null;
                  targetSquare = null;
                  selected = null;
               }
            }
         });
      
      for (int i = 0; i < 64; i ++) {
         SQUARES[i].click();
         SQUARES[i].drag();
         SQUARES[i].press();
      }
   }
   
   private void outline() {
      
      // left
      
      StackPane p1 = new StackPane();
      
      StackPane.setAlignment(box1, Pos.CENTER);
      
      r1.setFill(Color.SADDLEBROWN);
      
      box1.setSpacing(30);
      box1.setAlignment(Pos.CENTER);
      box1.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 20");
      
      for (int i = 8; i > 0; i --) {
         Label l = new Label("" + i);
         
         box1.getChildren().add(l);
      }
      
      p1.getChildren().addAll(r1, box1);
      
      // right
      
      StackPane p3 = new StackPane();
      
      StackPane.setAlignment(box3, Pos.CENTER);
      
      r3.setFill(Color.SADDLEBROWN);
      
      box3.setSpacing(30);
      box3.setAlignment(Pos.CENTER);
      box3.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 20");
      
      for (int i = 8; i > 0; i --) {
         Label l = new Label("" + i);
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
      box2.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 20");
      
      for (int i = 0; i < 8; i ++) {
         Label l = new Label(String.valueOf((char) (97 + i)));
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
      box4.setStyle("-fx-font-family: Times New Roman; -fx-font-size: 20");
      
      for (int i = 0; i < 8; i ++) {
         Label l = new Label(String.valueOf((char) (97 + i)));
         
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
      rootPane.setPadding(new Insets(50, 50, 50, 50));
      
      outline();
      
      for (int i = 0; i < 64; i ++) {
         SQUARES[i].display();
      }
      
      stack.getChildren().add(grid);
      
      for (Piece piece : pieces) {
         stack.getChildren().add(piece.get());
      }
      
      pane.setCenter(stack);
      
      rootPane.add(pane, 0, 0);
   }
   
   public void flip() {
      pane.setRotate(pane.getRotate() + 180);
      
      for (Piece piece : pieces) {
         piece.flip();
      }
   }
}




