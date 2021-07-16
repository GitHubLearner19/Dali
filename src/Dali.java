/**
 * Dali Chess Engine
 * 
 * 
 */ 

import java.util.*;
 
public class Dali {
   private static Map<Character, Integer> scores = new HashMap<>();
   private static Map<Character, int[]> pst = new HashMap<>();
   private static Table transpositions = new Table(25);
   private final static byte EXACT = 0;
   private final static byte LOWERBOUND = 1;
   private final static byte UPPERBOUND = 2;
   
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
      int alphaStart = alpha;
      
      // check transposition table for matches
      
      long hash = node.hash();
      Table.Entry entry = transpositions.get(hash);
      if (entry != null && entry.key() == hash && entry.depth() >= depth) {
         if (entry.flag() == EXACT) {
            return entry.score();
         } else if (entry.flag() == LOWERBOUND) {
            alpha = Math.max(alpha, entry.score());
         } else if (entry.flag() == UPPERBOUND) {
            beta = Math.min(beta, entry.score());
         }
      }
      
      if (alpha >= beta) {
         return entry.score();
      }
      
      // terminal node
      
      if (node.inCheckmate()) {
         return -20000;
      } else if (node.inDraw()) {
         return 0;
      } else if (depth == 0) {
         return evaluate(node) * color;
      }
      
      // search
      
      Move[] moves = node.moves();
      Move m = moves[0];
      int value = -20000;
      
      for (Move move : moves) {
         Chess chess = node.copy();
         chess.move(move);
         
         int score = -negamax(chess, depth - 1, -beta, -alpha, -color);
         
         if (score > value) {
            value = score;
            m = move;
            alpha = Math.max(alpha, value);         
         }
         
         if (alpha >= beta) {
            break;
         }
      }
      
      // add to table
      
      byte flag;
      
      if (value <= alphaStart) {
         flag = UPPERBOUND;
      } else if (value >= beta) {
         flag = LOWERBOUND;
      } else {
         flag = EXACT;
      }
      
      transpositions.put(node, m, depth, value, flag);
      
      return value;
   }
   
   public static Move[] search(Chess state) {
      Move[] moves = new Move[1];
      negamax(state, 4, -20000, 20000, state.getTurn() == 'w' ? 1 : -1);
      moves[0] = transpositions.get(state).move();
      transpositions.clear();
      return moves;
   }
}





