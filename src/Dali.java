/**
 * Dali Chess Engine
 * 
 * 
 */ 

import java.util.*;
 
public class Dali {
   private static Map<Character, Integer> scores = new HashMap<>();
   private static Map<Character, int[]> pst = new HashMap<>();
   
   static {
      scores.put('P', 100);
      scores.put('p', -100);
      scores.put('N', 320);
      scores.put('n', -320);
      scores.put('B', 330);
      scores.put('b', -330);
      scores.put('R', 500);
      scores.put('r', -500);
      scores.put('Q', 900);
      scores.put('q', -900);
      scores.put('K', 20000);
      scores.put('k', -20000);
      
      int[] pawns = { 
          0,  0,  0,  0,  0,  0,  0,  0,
         50, 50, 50, 50, 50, 50, 50, 50,
         10, 10, 20, 30, 30, 20, 10, 10,
          5,  5, 10, 25, 25, 10,  5,  5,
          0,  0,  0, 20, 20,  0,  0,  0,
          5, -5,-10,  0,  0,-10, -5,  5,
          5, 10, 10,-20,-20, 10, 10,  5,
          0,  0,  0,  0,  0,  0,  0,  0
      };
      pawns = arrange(pawns);
      
      int[] knights = {
         -50,-40,-30,-30,-30,-30,-40,-50,
         -40,-20,  0,  0,  0,  0,-20,-40,
         -30,  0, 10, 15, 15, 10,  0,-30,
         -30,  5, 15, 20, 20, 15,  5,-30,
         -30,  0, 15, 20, 20, 15,  0,-30,
         -30,  5, 10, 15, 15, 10,  5,-30,
         -40,-20,  0,  5,  5,  0,-20,-40,
         -50,-40,-30,-30,-30,-30,-40,-50
      };
      knights = arrange(knights);
      
      int[] bishops = {
         -20,-10,-10,-10,-10,-10,-10,-20,
         -10,  0,  0,  0,  0,  0,  0,-10,
         -10,  0,  5, 10, 10,  5,  0,-10,
         -10,  5,  5, 10, 10,  5,  5,-10,
         -10,  0, 10, 10, 10, 10,  0,-10,
         -10, 10, 10, 10, 10, 10, 10,-10,
         -10,  5,  0,  0,  0,  0,  5,-10,
         -20,-10,-10,-10,-10,-10,-10,-20
      };
      bishops = arrange(bishops);
      
      int[] rooks = {
        0,  0,  0,  0,  0,  0,  0,  0,
        5, 10, 10, 10, 10, 10, 10,  5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
       -5,  0,  0,  0,  0,  0,  0, -5,
        0,  0,  0,  5,  5,  0,  0,  0
      };
      rooks = arrange(rooks);
      
      int[] queens = {
         -20,-10,-10, -5, -5,-10,-10,-20,
         -10,  0,  0,  0,  0,  0,  0,-10,
         -10,  0,  5,  5,  5,  5,  0,-10,
          -5,  0,  5,  5,  5,  5,  0, -5,
           0,  0,  5,  5,  5,  5,  0, -5,
         -10,  5,  5,  5,  5,  5,  0,-10,
         -10,  0,  5,  0,  0,  0,  0,-10,
         -20,-10,-10, -5, -5,-10,-10,-20
      };
      queens = arrange(queens);
      
      int[] kings = {
         -30,-40,-40,-50,-50,-40,-40,-30,
         -30,-40,-40,-50,-50,-40,-40,-30,
         -30,-40,-40,-50,-50,-40,-40,-30,
         -30,-40,-40,-50,-50,-40,-40,-30,
         -20,-30,-30,-40,-40,-30,-30,-20,
         -10,-20,-20,-20,-20,-20,-20,-10,
          20, 20,  0,  0,  0,  0, 20, 20,
          20, 30, 10,  0,  0, 10, 30, 20
      };
      kings = arrange(kings);
      
      pst.put('P', pawns);
      pst.put('p', flip(pawns));
      pst.put('N', knights);
      pst.put('n', flip(knights));
      pst.put('B', bishops);
      pst.put('b', flip(bishops));
      pst.put('R', rooks);
      pst.put('r', flip(rooks));
      pst.put('Q', queens);
      pst.put('q', flip(queens));
      pst.put('K', kings);
      pst.put('k', flip(kings));
   }
   
   private static int[] arrange(int[] table) {
      int[] t = new int[64];
      
      for (int i = 0; i < table.length; i ++) {
         t[i] = table[8 * (7 - i / 8) + i % 8];
      }
      
      return t;
   }
   
   private static int[] flip(int[] table) {
      int[] t = new int[64];
      
      for(int i = 0; i < table.length; i++) {
          t[i] = -table[table.length - i - 1];
      }
      
      return t;
   }
   
   public static int evaluate(Chess state) {
      ArrayList<Chess.PieceSquare> board = state.board();
      int score = 0;
      
      for (Chess.PieceSquare entry : board) {
         score += scores.get(entry.getPiece()) + pst.get(entry.getPiece())[entry.getSquare()];
      }
      
      return score;
   }
   
   public static int negamax(Chess node, int depth, int alpha, int beta, int color) {
      if (node.inCheckmate()) {
         return Integer.MAX_VALUE * color;
      } else if (node.inDraw()) {
         return 0;
      } else if (depth == 0) {
         return evaluate(node) * color;
      }
      
      Move[] moves = node.moves();
      int value = -Integer.MAX_VALUE;
      
      for (Move move : moves) {
         Chess chess = node.copy();
         chess.move(move);
         value = Math.max(value, -negamax(chess, depth - 1, -beta, -alpha, -color));
         alpha = Math.max(alpha, value);
         
         if (alpha >= beta) {
            break;
         }
      }
      
      return value;
   }
   
   public static Move[] negamaxRoot(Chess node, int depth) {
      Move[] moves = node.moves();
      int color = node.getTurn() == 'w' ? 1 : -1;
      ArrayList<Move> top = new ArrayList<>();
      int score = -Integer.MAX_VALUE;
      
      for (Move move : moves) {
         Chess chess = node.copy();
         chess.move(move);

         int temp = -negamax(chess, depth - 1, -Integer.MAX_VALUE, Integer.MAX_VALUE, -color);
         if (temp >= score) {
            if (temp > score) {
               top.clear();
            }
            score = temp;
            top.add(move);
         }
      }
      
      return top.toArray(new Move[0]);
   }
   
   public static Move[] search(Chess state) {
      Move[] result = new Move[1];
      Move[] options = negamaxRoot(state, 4);
      
      result[0] = options[(int) (Math.random() * options.length)];
      
      return result;
   }
}




