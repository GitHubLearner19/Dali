/** 
 *
 * 
 */

public class Move {
   private int data;
   private static final char[] CASTLE = {'n', 'K', 'Q', 'k', 'q'};
   private static final char[] PROMOTE = {'x', 'N', 'B', 'R', 'Q', 'n', 'b', 'r', 'q'};
   
   public Move(int start, int target, char castle, char promote, boolean dblp, boolean ep) {
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
      
      data |= (dblp ? 1 : 0) << 20;
      data |= (ep ? 1 : 0) << 21;
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
      return PROMOTE[extract(15, 5)];
   }
   
   public boolean getDoublePawnPush() {
      return BS.getBit(data, 20) == 1;
   }
   
   public boolean getEnPassant() {
      return BS.getBit(data, 21) == 1;
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



