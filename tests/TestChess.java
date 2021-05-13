

public class TestChess {
   public static void main(String[] args) {
      Chess chess = new Chess();
      System.out.println(chess);
      
      String square = "b1";
      
      Move[] m = chess.moves(square);
      
      System.out.print("moves from " + square + ": ");
      
      for (Move move : m) {
         System.out.print(move + " ");
      }
      
      System.out.println("\n");
      
      int depth = 5;
      
      long startTime = System.currentTimeMillis();
      
      long nodes = chess.perft(depth);
      
      System.out.println("depth " + depth + ": " + nodes + " nodes");
      
      long elapsed = System.currentTimeMillis() - startTime;
      
      System.out.println("time elapsed: " + elapsed + "ms");
      
      System.out.println(nodes / elapsed + "kN/s");
   }
}


