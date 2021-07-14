/**
 * Transposition Table
 *
 */

public class Table {
   private Entry[] entries;
   private int size;
   
   // table entries
   class Entry {
      private long key;
      private Move move;
      private int depth;
      private int score;
      private byte flag;
      
      public Entry(long key, Move move, int depth, int score, byte flag) {
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
      
      public byte flag() {
         return flag;
      }
   }
   
   public Table(int size) {
      this.size = size;
      entries = new Entry[(int) Math.pow(2, size)];
   }
   
   // add entry
   public void put(Chess chess, Move move, int depth, int score, byte flag) {
      long hash = chess.hash();
      int index = Math.floorMod(hash, entries.length);
      
      // replace by depth
      if (entries[index] == null || depth > entries[index].depth()) {
         entries[index] = new Entry(hash, move, depth, score, flag);
      }
   }
   
   // get entry
   public Entry get(long hash) {
      return entries[Math.floorMod(hash, entries.length)];
   }
   
   // get entry
   public Entry get(Chess chess) {
      return entries[Math.floorMod(chess.hash(), entries.length)];
   }
   
   public void clear() {
      entries = new Entry[(int) Math.pow(2, size)];
   }
}







