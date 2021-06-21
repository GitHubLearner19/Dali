/** 
 *
 * 
 */

public class Move {
   private int data;
   private static final char[] CASTLE = {'n', 'K', 'Q', 'k', 'q'};
   private static final char[] PROMOTE = {'x', 'N', 'B', 'R', 'Q', 'n', 'b', 'r', 'q'};
   private static final char[] CAPTURE = {'x', 'P', 'N', 'B', 'R', 'Q', 'p', 'n', 'b', 'r', 'q'};
   
   public Move(int start, int target, char castle, char promote, char capture, boolean dblp, boolean ep) {
      data = start | (target << 6);
      
      switch (castle) {
         case 'K':
            data |= 1 << 12;
            break;
         case 'Q':
            data |= 2 << 12;
            break;
         case 'k':
            data |= 3 << 12;
            break;
         case 'q':
            data |= 4 << 12;
      }
      
      switch (promote) {
         case 'N':
            data |= 1 << 15;
            break;
         case 'B':
            data |= 2 << 15;
            break;
         case 'R':
            data |= 3 << 15;
            break;
         case 'Q':
            data |= 4 << 15;
            break;
         case 'n':
            data |= 5 << 15;
            break;
         case 'b':
            data |= 6 << 15;
            break;
         case 'r':
            data |= 7 << 15;
            break;
         case 'q':
            data |= 8 << 15;
      }
      
      setCapture(capture);
      
      data |= (dblp ? 1 : 0) << 24;
      data |= (ep ? 1 : 0) << 25;
   }
   
   public Move(int data) {
      this.data = data;
   }
   
   // extract k bits starting at position s
   private int extract(int s, int k) {
      return ((1 << k) - 1) & (data >>> s);
   }
   
   // return all data associated with move
   public int get() {
      return data;
   }
   
   public int getStart() {
      return extract(0, 6);
   }
   
   public int getTarget() {
      return extract(6, 6);
   }
   
   public char getCastle() {
      return CASTLE[extract(12, 3)];
   }
   
   public char getPromote() {
      return PROMOTE[extract(15, 4)];
   }
   
   public char getCapture() {
      return CAPTURE[extract(19, 5)];
   }
   
   public boolean getDoublePawnPush() {
      return BS.getBit(data, 24) == 1;
   }
   
   public boolean getEnPassant() {
      return BS.getBit(data, 25) == 1;
   }
   
   public void setCapture(char capture) {
      switch (capture) {
         case 'P':
            data |= 1 << 19;
            break;
         case 'N':
            data |= 2 << 19;
            break;
         case 'B':
            data |= 3 << 19;
            break;
         case 'R':
            data |= 4 << 19;
            break;
         case 'Q':
            data |= 5 << 19;
            break;
         case 'p':
            data |= 6 << 19;
            break;
         case 'n':
            data |= 7 << 19;
            break;
         case 'b':
            data |= 8 << 19;
            break;
         case 'r':
            data |= 9 << 19;
            break;
         case 'q':
            data |= 10 << 19;
      }
   }
   
   @Override
   public String toString() {
      String message = "";
      
      message += Chess.toSquare(getStart()) + Chess.toSquare(getTarget());
      
      char promote = getPromote();
      
      if (promote != 'x') {
         message += promote;
      }
      
      return  message;
   }
}



