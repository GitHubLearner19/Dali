/**
 * Transposition Table
 *
 */

public class Table {
   private Entry[] entries;
   
   // table entries
   class Entry {
      private long key;
      private Move move;
      private int depth;
      private int score;
      private char flag;
      
      public Entry(long key, Move move, int depth, int score, char flag) {
         this.key = key;
         this.move = move;
         this.depth = depth; 
         this.score = score;
         this.flag = flag;
      }
      
      public long key() {
         return key;
      }
      
      public Move move() {
         return move;
      }
      
      public int depth() {
         return depth;
      }
      
      public int score() {
         return score;
      }
      
      public char flag() {
         return flag;
      }
   }
   
   public Table(int size) {
      entries = new Entry[(int) Math.pow(2, size)];
   }
   
   // add entry
   public void put(Chess chess, Move move, int depth, int score, char flag) {
      long hash = chess.hash();
      int index = (int) (hash % entries.length);
      
      // replace by depth
      if (entries[index] == null || depth > entries[index].depth()) {
         entries[index] = new Entry(hash, move, depth, score, flag);
      }
   }
   
   // get entry
   public Entry get(Chess chess) {
      return entries[(int) (chess.hash() % entries.length)];
   }
}







