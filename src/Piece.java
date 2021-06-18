/**
 *
 *
 *
 */
 
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Group;
import javafx.animation.PathTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.shape.Line;
import javafx.geometry.Pos;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;

public class Piece {
   private ImageView img = new ImageView();
   private double size = 50;
   
   private double startX;
   private double startY;
   private int column;
   private int row;
   
   public Piece(char p) {
      set(p);
      img.setMouseTransparent(true);
      img.setPreserveRatio(true);
      img.setFitHeight(size);
      
      startX = size / 2;
      startY = size / 2;
      column = 0;
      row = 0;
   }
   
   public Piece(char p, int column, int row) {
      this(p);
      startX = column * size + size / 2;
      startY = row * size + size / 2;
      img.setTranslateX(startX - size / 2);
      img.setTranslateY(startY - size / 2);
      this.column = column;
      this.row = row;
   }
   
   public void move(int column, int row, EventHandler<ActionEvent> handler) {
      double targetX = column * size + size / 2;
      double targetY = row * size + size / 2;
      
      this.column = column;
      this.row = row;
      
      PathTransition pt = new PathTransition(Duration.millis(200), new Line(startX, startY, targetX, targetY), img);
      pt.setCycleCount(1);
      
      if (startX == targetX && startY == targetY) {
         handler.handle(new ActionEvent());
      } else {
         pt.play();
         pt.setOnFinished(handler);
         startX = column * size + size / 2;
         startY = row * size + size / 2;
      }
   }
   
   public void fade(EventHandler<ActionEvent> handler) {
      FadeTransition ft = new FadeTransition(Duration.millis(200), img);
      ft.setFromValue(1.0);
      ft.setToValue(0);
      ft.setCycleCount(1);
      ft.play();
      ft.setOnFinished(handler);
   }
   
   public int getColumn() {
      return column;
   }
   
   public int getRow() {
      return row;
   }
   
   public double getX() {
      return startX;
   }
   
   public void setX(double x) {
      startX = x;
      img.setTranslateX(x - size / 2);
   }
   
   public void setY(double y) {
      startY = y;
      img.setTranslateY(y - size / 2);
   }
   
   public double getY() {
      return startY;
   }
   
   public void flip() {
      img.setRotate(img.getRotate() + 180);
   }
   
   public void set(char p) {
      String filename = "images/pieces/" + (Character.isUpperCase(p) ? "White" : "Black");
      
      switch (Character.toLowerCase(p)) {
         case 'p':
            filename += "Pawn";
            break;
         case 'n':
            filename += "Knight";
            break;
         case 'b': 
            filename += "Bishop";
            break;
         case 'r':
            filename += "Rook";
            break;
         case 'q': 
            filename += "Queen";
            break;
         case 'k':
            filename += "King";
      }
      
      img.setImage(new Image(filename + ".png", 0, 100, true, true));
   }
   
   public ImageView get() {
      return img;
   }
}




