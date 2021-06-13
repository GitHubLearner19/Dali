/**
 *
 *
 *
 */
 
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.PathTransition;
import javafx.util.Duration;
import javafx.scene.shape.Line;

public class Piece {
   private ImageView img;
   private int size = 50;
   
   private double startX;
   private double startY;
   
   public Piece(char p) {
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
      
      img = new ImageView(new Image(filename + ".png", 0, size, true, true));
      img.setMouseTransparent(true);
      startX = size / 2;
      startY = size / 2;
   }
   
   public Piece(char p, int column, int row) {
      this(p);
      startX = (column + 0.5) * size;
      startY = (row + 0.5) * size;
      img.setTranslateX(startX);
      img.setTranslateY(startY);
   }
   
   public void move(int row, int column) {
      PathTransition pt = new PathTransition(Duration.millis(1000), new Line(startX, startY, (column + 0.5) * size, (row + 0.5) * size), img);
      pt.setCycleCount(1);
      pt.play();
   }
   
   public void setX(int x) {
      startX = x;
   }
   
   public void setY(int y) {
      startY = y;
   }
   
   public void setSize(int s) {
      size = s;
      
   }
   
   public ImageView get() {
      return img;
   }
}




