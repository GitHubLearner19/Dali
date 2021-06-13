/**
 *
 *
 *
 */

public class TestChess {
   public static void main(String[] args) {
      Chess chess = new Chess("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1");
      System.out.println(chess);
      
      String square = "d2";
      
      Move[] m = chess.moves(square);
      
      System.out.print("moves from " + square + ": ");
      
      for (Move move : m) {
         System.out.print(move + " ");
      }
      
      System.out.println("\n");
      
      int depth = 4;
      
      long startTime = System.currentTimeMillis();
      
      long nodes = chess.perft(depth);
      
      System.out.println("depth " + depth + ": " + nodes + " nodes");
      
      long elapsed = System.currentTimeMillis() - startTime;
      
      System.out.println("time elapsed: " + elapsed + "ms");
      
      System.out.println(nodes / elapsed + "kN/s");
   }
}


