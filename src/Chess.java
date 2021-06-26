/**
 *
 *
 *
 */

import java.util.*;

/* Chess Board */

public class Chess {
   
   /** piece bitboards */
   
   private long wPawns;
   private long bPawns;
   
   private long wKnights;
   private long bKnights;
   
   private long wBishops;
   private long bBishops;
   
   private long wRooks;
   private long bRooks;
   
   private long wQueens;
   private long bQueens;
   
   private long wKing;
   private long bKing;
   
   // turn w true b false
   private boolean turn;
   
   // castling rights
   private boolean[] castle;
   
   // en passant capture square
   private int epsquare;
   
   // 50 move rule
   private int movesSinceCapture;
   
   private int fullMoves;
   
   // history
   private ArrayList<Move> history = new ArrayList<>();
   private ArrayList<Boolean[]> totalCastles = new ArrayList<>();
   private ArrayList<Integer> totalEpSquares = new ArrayList<>();
   private ArrayList<Integer> totalHalfMoves = new ArrayList<>();
   private boolean historyOn = false;
   
   // pgn
   private ArrayList<String> pgn = new ArrayList<>();
   
   // compass directions
   
   private final static int NORT = 0;
   private final static int EAST = 1;
   private final static int SOUT = 2;
   private final static int WEST = 3;
   private final static int NOEA = 4;
   private final static int SOEA = 5;
   private final static int SOWE = 6;
   private final static int NOWE = 7;
   
   // common bitboards
   private final static long MASKA = 0xFEFEFEFEFEFEFEFEL;
   private final static long MASKB = 0xFDFDFDFDFDFDFDFDL;
   private final static long MASKG = 0XBFBFBFBFBFBFBFBFL;
   private final static long MASKH = 0x7F7F7F7F7F7F7F7FL;
   private final static long MASKR1 = 0xFFFFFFFFFFFFFF00L;
   private final static long MASKR8 = 0x00FFFFFFFFFFFFFFL;
   private final static long LIGHTSQUARES = 0x55AA55AA55AA55AAL;
   private final static long DARKSQUARES = 0xAA55AA55AA55AA55L;
   
   // universe bitboard
   private final static long UNIVERSE = 0xFFFFFFFFFFFFFFFFL;
   
   private static long[][] RAYS = new long[8][64]; // attack rays
   private static long[] DIAGONALS = new long[64];
   private static long[] ANTIDIAGONALS = new long[64];
   private static long[] FILES = new long[64];
   private static long[] RANKS = new long[64];
   
   /* initialize rays */
   
   static {
      // orthogonals
      
      long north = 0x0101010101010100L;
      
      for (int i = 0; i < 64; i ++) {
         RAYS[NORT][i] = north;
         north <<= 1;
      }
      
      long east = 0xFE00000000000000L;
      
      for (int i = 0; i < 8; i ++) {
         long east2 = east;
         
         for (int j = 7; j >= 0; j --) {
            RAYS[EAST][j * 8 + i] = east2;
            east2 >>>= 8;
         }
         
         east <<= 1;
      }
      
      long south = 0x0080808080808080L;
      
      for (int i = 63; i >= 0; i --) {
         RAYS[SOUT][i] = south;
         south >>>= 1;
      }
      
      long west = 0x000000000000007FL;
      
      for (int i = 7; i >= 0; i --) {
         long west2 = west;
         
         for (int j = 0; j < 8; j ++) {
            RAYS[WEST][j * 8 + i] = west2;
            west2 <<= 8;
         }
         
         west >>>= 1;
      }
      
      // diagonals
      
      long northeast = 0x8040201008040200L;
      
      for (int i = 0; i < 8; i ++) {
         long northeast2 = northeast;
         
         for (int j = 0; j < 8; j ++) {
            RAYS[NOEA][j * 8 + i] = northeast2;
            northeast2 <<= 8;
         }
         
         northeast = (northeast << 1) & MASKA;
      }
      
      long southeast = 0x0002040810204080L;
      
      for (int i = 0; i < 8; i ++) {
         long southeast2 = southeast;
         
         for (int j = 7; j >= 0; j --) {
            RAYS[SOEA][j * 8 + i] = southeast2;
            southeast2 >>>= 8;
         }
         
         southeast = (southeast << 1) & MASKA;
      }
      
      long southwest = 0x0040201008040201L;
      
      for (int i = 7; i >= 0; i --) {
         long southwest2 = southwest;
         
         for (int j = 7; j >= 0; j --) {
            RAYS[SOWE][j * 8 + i] = southwest2;
            southwest2 >>>= 8;
         }
         
         southwest = (southwest >>> 1) & MASKH;
      }
      
      long northwest = 0x0102040810204000L;
      
      for (int i = 7; i >= 0; i --) {
         long northwest2 = northwest;
         
         for (int j = 0; j < 8; j ++) {
            RAYS[NOWE][j * 8 + i] = northwest2;
            northwest2 <<= 8;
         }
         
         northwest = (northwest >>> 1) & MASKH;
      }
      
      for (int i = 0; i < 64; i ++) {
         DIAGONALS[i] = RAYS[NOEA][i] | RAYS[SOWE][i];
         ANTIDIAGONALS[i] = RAYS[NOWE][i] | RAYS[SOEA][i];
         FILES[i] = RAYS[NORT][i] | RAYS[SOUT][i];
         RANKS[i] = RAYS[EAST][i] | RAYS[WEST][i];
      }
   }
   
   private static long[] PAWNATTACKSW = new long[64]; // pawn attacks
   private static long[] PAWNATTACKSB = new long[64];
   
   private static long[] KNIGHTATTACKS = new long[64]; // knight attacks
   private static long[] KINGATTACKS = new long[64]; // king attacks
   
   static {
      long maskAB = MASKA & MASKB;
      long maskGH = MASKG & MASKH;
      
      for (int i = 0; i < 64; i ++) {
         long bb = 1L << i;
         
         KNIGHTATTACKS[i] = bb << 17 & MASKA | bb << 10 & maskAB | bb >>> 6 & maskAB | bb >>> 15 & MASKA | bb << 15 & MASKH | bb << 6 & maskGH | bb >>> 10 & maskGH | bb >>> 17 & MASKH;
         PAWNATTACKSW[i] = bb << 7 & MASKH | bb << 9 & MASKA;
         PAWNATTACKSB[i] = bb >>> 7 & MASKA | bb >>> 9 & MASKH;
         
         long attacks = bb << 1 & MASKA | bb >>> 1 & MASKH;
         bb |= attacks;
         attacks |= bb << 8 | bb >>> 8;
         
         KINGATTACKS[i] = attacks;
      }
   }
   
   public Chess() {
      this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
   }
   
   public Chess(boolean hOn) {
      this();
      historyOn = hOn;
   }
   
   public Chess(String fen) {
      if (fen.equals("empty")) {
         clear();
         turn = true;
         castle = new boolean[4];
         castle[0] = true;
         castle[1] = true;
         castle[2] = true;
         castle[3] = true;
         epsquare = 0;
         movesSinceCapture = 0;
         fullMoves = 1;
      } else {
         set(fen);
      }
   }
   
   public Chess(String fen, boolean hOn) {
      this(fen);
      historyOn = hOn;
   }
   
   private Chess(long wP, long bP, long wN, long bN, long wB, long bB, long wR, long bR, long wQ, long bQ, long wK, long bK, boolean turn, boolean[] castle, int ep, int m1, int m2) {
      wPawns = wP;
      bPawns = bP;
      wKnights = wN;
      bKnights = bN;
      wBishops = wB;
      bBishops = bB;
      wRooks = wR;
      bRooks = bR;
      wQueens = wQ;
      bQueens = bQ;
      wKing = wK;
      bKing = bK;
      this.turn = turn;
      this.castle = castle;
      epsquare = ep;
      movesSinceCapture = m1;
      fullMoves = m2;
   }
   
   public void reset() {
      set("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");
   }
   
   public void clear() {
      wPawns = 0;
      bPawns = 0;
      wKnights = 0;
      bKnights = 0;
      wBishops = 0;
      bBishops = 0;
      wRooks = 0;
      bRooks = 0;
      wQueens = 0;
      bQueens = 0;
      wKing = 0;
      bKing = 0;
   }
   
   public class PieceSquare {
      private char piece;
      private int square;
      
      public PieceSquare(char piece, int square) {
         this.piece = piece;
         this.square = square;
      }
      
      public char getPiece() {
         return piece;
      }
      
      public int getSquare() {
         return square;
      }
   }
   
   private class MovePair {
      private char piece;
      private long moves;
      
      public MovePair(char piece, long moves) {
         setPiece(piece);
         setMoves(moves);
      }
      
      public void setPiece(char piece) {
         this.piece = piece;
      }
      
      public void setMoves(long moves) {
         this.moves = moves;
      }
      
      public char getPiece() {
         return piece;
      }
      
      public long getMoves(){ 
         return moves;
      }
   }
   
   private void setTurn(boolean turn)  {
      this.turn = turn;
   }
   
   // piece at square
   public char pieceAt(int square) {
      if (BS.getBit(wPawns, square) == 1) {
         return 'P';
      } else if (BS.getBit(bPawns, square) == 1) {
         return 'p';
      } else if (BS.getBit(wKnights, square) == 1) {
         return 'N';
      } else if (BS.getBit(bKnights, square) == 1) {
         return 'n';
      } else if (BS.getBit(wBishops, square) == 1) {
         return 'B';
      } else if (BS.getBit(bBishops, square) == 1) {
         return 'b';
      } else if (BS.getBit(wRooks, square) == 1) {
         return 'R';
      } else if (BS.getBit(bRooks, square) == 1) {
         return 'r';
      } else if (BS.getBit(wQueens, square) == 1) {
         return 'Q';
      } else if (BS.getBit(bQueens, square) == 1) {
         return 'q';
      } else if (BS.getBit(wKing, square) == 1) {
         return 'K';
      } else if (BS.getBit(bKing, square) == 1) {
         return 'k';
      } else {
         return 'x';
      }
   }

   // debugging
   private static void printBitboard(long bb) {
      String board = "";
      
      for (int i = 7; i >= 0; i --) {
         for (int j = 0; j < 8; j ++) {
            board += BS.getBit(bb, i * 8 + j);
         }
         
         board += "\n";
      }
      
      System.out.println(board);
   }
   
   /** sliding piece moves */
   
   private long diagonalMoves(int square, long blockers) {
      long maskedBlockers = RAYS[NOEA][square] & blockers;
      int blocksquare = BS.LSB(maskedBlockers);
      
      long m1 = RAYS[NOEA][square];
      
      if (blocksquare >= 0) {
         m1 ^= RAYS[NOEA][blocksquare];
      }
      
      maskedBlockers = RAYS[SOWE][square] & blockers;
      blocksquare = BS.MSB(maskedBlockers);
      
      long m2 = RAYS[SOWE][square];
      
      if (blocksquare >= 0) {
         m2 ^= RAYS[SOWE][blocksquare];
      }
      
      return m1 | m2;
   }
   
   private long antidiagonalMoves(int square, long blockers) {
      long maskedBlockers = RAYS[NOWE][square] & blockers;
      int blocksquare = BS.LSB(maskedBlockers);
      
      long m1 = RAYS[NOWE][square];
      
      if (blocksquare >= 0) {
         m1 ^= RAYS[NOWE][blocksquare];
      }
      
      maskedBlockers = RAYS[SOEA][square] & blockers;
      blocksquare = BS.MSB(maskedBlockers);
      
      long m2 = RAYS[SOEA][square];
      
      if (blocksquare >= 0) {
         m2 ^= RAYS[SOEA][blocksquare];
      }
      
      return m1 | m2;
   }
   
   private long fileMoves(int square, long blockers) {
      long maskedBlockers = RAYS[NORT][square] & blockers;
      int blocksquare = BS.LSB(maskedBlockers);
      
      long m1 = RAYS[NORT][square];
      
      if (blocksquare >= 0) {
         m1 ^= RAYS[NORT][blocksquare];
      }
      
      maskedBlockers = RAYS[SOUT][square] & blockers;
      blocksquare = BS.MSB(maskedBlockers);
      
      long m2 = RAYS[SOUT][square];
      
      if (blocksquare >= 0) {
         m2 ^= RAYS[SOUT][blocksquare];
      }
      
      return m1 | m2;
   }
   
   private long rankMoves(int square, long blockers) {
      long maskedBlockers = RAYS[EAST][square] & blockers;
      int blocksquare = BS.LSB(maskedBlockers);
      
      long m1 = RAYS[EAST][square];
      
      if (blocksquare >= 0) {
         m1 ^= RAYS[EAST][blocksquare];
      }
      
      maskedBlockers = RAYS[WEST][square] & blockers;
      blocksquare = BS.MSB(maskedBlockers);
      
      long m2 = RAYS[WEST][square];
      
      if (blocksquare >= 0) {
         m2 ^= RAYS[WEST][blocksquare];
      }
      
      return m1 | m2;
   }
   
   /* occupied squares on bitboard */
   
   private ArrayList<Integer> occupiedSquares(long bb) {
      ArrayList<Integer> occupiedSquares = new ArrayList<>();
      
      int square = 0;
      
      while (bb != 0) {
         square = BS.LSB(bb);
         occupiedSquares.add(square);
         bb ^= 1L << square;
      }
      
      return occupiedSquares;
   }
   
   /* attack squares */
   
   private long[] getSlidingAttacksWhite(long blockers) {
      long[] attacks = new long[4];
      
      for (Integer square : occupiedSquares(wBishops)) {
         attacks[0] |= diagonalMoves(square, blockers);
         attacks[1] |= antidiagonalMoves(square, blockers);
      }
      
      for (Integer square : occupiedSquares(wRooks)) {
         attacks[2] |= fileMoves(square, blockers);
         attacks[3] |= rankMoves(square, blockers);
      }
      
      for (Integer square : occupiedSquares(wQueens)) {
         attacks[0] |= diagonalMoves(square, blockers);
         attacks[1] |= antidiagonalMoves(square, blockers);
         attacks[2] |= fileMoves(square, blockers);
         attacks[3] |= rankMoves(square, blockers);
      }
      
      return attacks;
   }
   
   private long[] getSlidingAttacksBlack(long blockers) {
      long[] attacks = new long[4];
      
      for (Integer square : occupiedSquares(bBishops)) {
         attacks[0] |= diagonalMoves(square, blockers);
         attacks[1] |= antidiagonalMoves(square, blockers);
      }
      
      for (Integer square : occupiedSquares(bRooks)) {
         attacks[2] |= fileMoves(square, blockers);
         attacks[3] |= rankMoves(square, blockers);
      }
      
      for (Integer square : occupiedSquares(bQueens)) {
         attacks[0] |= diagonalMoves(square, blockers);
         attacks[1] |= antidiagonalMoves(square, blockers);
         attacks[2] |= fileMoves(square, blockers);
         attacks[3] |= rankMoves(square, blockers);
      }
      
      return attacks;
   }
   
   private long getNonSlidingAttacksWhite(long blockers) {
      long attacks = 0L;
      
      for (Integer square : occupiedSquares(wPawns)) {
         attacks |= getPawnCapturesWhite(square, UNIVERSE);
      }
      
      for (Integer square : occupiedSquares(wKnights)) {
         attacks |= getKnightMoves(square);
      }
      
      attacks |= getKingMoves(BS.LSB(wKing), 0L);
      
      return attacks;
   }
   
   private long getNonSlidingAttacksBlack(long blockers) {
      long attacks = 0L;
      
      for (Integer square : occupiedSquares(bPawns)) {
         attacks |= getPawnCapturesBlack(square, UNIVERSE);
      }
      
      for (Integer square : occupiedSquares(bKnights)) {
         attacks |= getKnightMoves(square);
      }
      
      attacks |= getKingMoves(BS.LSB(bKing), 0L);
      
      return attacks;
   }
   
   /* psuedo-legal move generation */
   
   private long getPawnPushesWhite(int square, long empty) {
      long pushes = 1L << (square + 8) & empty;
      
      return square > 15 ? pushes : (pushes | (pushes << 8) & empty);
   }
   
   private long getPawnPushesBlack(int square, long empty) {
      long pushes = 1L << (square - 8) & empty;
      
      return square < 48 ? pushes : (pushes | (pushes >>> 8) & empty);
   }
   
   private long getPawnCapturesWhite(int square, long piecesbb) {
      return PAWNATTACKSW[square] & piecesbb;
   }
   
   private long getPawnCapturesBlack(int square, long piecesbb) {
      return PAWNATTACKSB[square] & piecesbb;
   }
   
   private long getKnightMoves(int square) {
      return KNIGHTATTACKS[square];
   }
   
   private long getBishopMoves(int square, long blockers) {
      return diagonalMoves(square, blockers) | antidiagonalMoves(square, blockers);
   }
   
   private long getRookMoves(int square, long blockers) {
      return fileMoves(square, blockers) | rankMoves(square, blockers);
   }
   
   private long getQueenMoves(int square, long blockers) {
      return getBishopMoves(square, blockers) | getRookMoves(square, blockers);
   }
   
   private long getKingMoves(int square, long attacked) {
      return KINGATTACKS[square] & ~attacked;
   }
   
   private ArrayList<Move> getKingCastlesWhite(long blockers, long attacked) {
      ArrayList<Move> m = new ArrayList<>();
      
      if (castle[0] && (0x60L & ~(blockers | attacked)) == 0x60L) {
         m.add(new Move(4, 6, 'K', 'x', 'x', false, false));
      }
      
      if (castle[1] && (0xCL & ~attacked) == 0xCL && (0xEL & ~blockers) == 0xEL) {
         m.add(new Move(4, 2, 'Q', 'x', 'x', false, false));
      }
      
      return m;
   }
   
   private ArrayList<Move> getKingCastlesBlack(long blockers, long attacked) {
      ArrayList<Move> m = new ArrayList<>();
      
      if (castle[2] && (0x6000000000000000L & ~(blockers | attacked)) == 0x6000000000000000L) {
         m.add(new Move(60, 62, 'k', 'x', 'x', false, false));
      }
      
      if (castle[3] && (0xC00000000000000L & ~attacked) == 0xC00000000000000L && (0xE00000000000000L & ~blockers) == 0xE00000000000000L) {
         m.add(new Move(60, 58, 'q', 'x', 'x', false, false));
      }
      
      return m;
   }
   
   private ArrayList<Move> getEPCapturesWhite(long epbb) {
      ArrayList<Move> m = new ArrayList<>();
      
      Move m1 = new Move(epsquare - 9, epsquare, 'n', 'x', 'p', false, true);
      
      if ((epbb >>> 9 & MASKH & wPawns) != 0) {
         Chess c = copy();
         c.move(m1);
         c.setTurn(true);
         
         if(!c.inCheck()) {
            m.add(m1);
         }
      }
      
      Move m2 = new Move(epsquare - 7, epsquare, 'n', 'x', 'p', false, true);
      
      if ((epbb >>> 7 & MASKA & wPawns) != 0) {
         Chess c = copy();
         c.move(m2);
         c.setTurn(true);
         
         if(!c.inCheck()) {
            m.add(m2);
         }
      }
      
      return m;
   }
   
   private ArrayList<Move> getEPCapturesBlack(long epbb) {
      ArrayList<Move> m = new ArrayList<>();
      
      Move m1 = new Move(epsquare + 7, epsquare, 'n', 'x', 'P', false, true);
      
      if ((epbb << 7 & MASKH & bPawns) != 0) {
         Chess c = copy();
         c.move(m1);
         c.setTurn(false);
         
         if(!c.inCheck()) {
            m.add(m1);
         }
      }
      
      Move m2 = new Move(epsquare + 9, epsquare, 'n', 'x', 'P', false, true);
      
      if ((epbb << 9 & MASKA & bPawns) != 0) {
         Chess c = copy();
         c.move(m2);
         c.setTurn(false);
         
         if(!c.inCheck()) {
            m.add(m2);
         }
      }
      
      return m;
   }
   
   // take hashmap of move squares to convert to move types
   private void createMoves(ArrayList<Move> m, HashMap<Integer, MovePair> moves) {
      for (Map.Entry<Integer, MovePair> entry : moves.entrySet()) {
         if (Character.toUpperCase(entry.getValue().getPiece()) == 'P') {
            for (Integer target : occupiedSquares(entry.getValue().getMoves())) {
               if (target > 55) {
                  m.add(new Move(entry.getKey(), target, 'n', 'N', 'x', false, false));
                  m.add(new Move(entry.getKey(), target, 'n', 'B', 'x', false, false));
                  m.add(new Move(entry.getKey(), target, 'n', 'R', 'x', false, false));
                  m.add(new Move(entry.getKey(), target, 'n', 'Q', 'x', false, false));
               } else if (target < 8) {
                  m.add(new Move(entry.getKey(), target, 'n', 'n', 'x', false, false));
                  m.add(new Move(entry.getKey(), target, 'n', 'b', 'x', false, false));
                  m.add(new Move(entry.getKey(), target, 'n', 'r', 'x', false, false));
                  m.add(new Move(entry.getKey(), target, 'n', 'q', 'x', false, false));
               } else if (Math.abs(target - entry.getKey()) == 16) {
                  m.add(new Move(entry.getKey(), target, 'n', 'x', 'x', true, false));
               } else {
                  m.add(new Move(entry.getKey(), target, 'n', 'x', 'x', false, false));
               }
            }
         } else {
            for (Integer target : occupiedSquares(entry.getValue().getMoves())) {
               m.add(new Move(entry.getKey(), target, 'n', 'x', 'x', false, false));
            }
         }
      }
   }
   
   // get pieces white can attack
   private char[] targetsWhite() {
      char[] targets = new char[64];
      Arrays.fill(targets, 'x');
      
      ArrayList<Integer> pt = occupiedSquares(bPawns);
      
      for (Integer i : pt) {
         targets[i] = 'p';
      }
      
      ArrayList<Integer> nt = occupiedSquares(bKnights);
      
      for (Integer i : nt) {
         targets[i] = 'n';
      }
      
      ArrayList<Integer> bt = occupiedSquares(bBishops);
      
      for (Integer i : bt) {
         targets[i] = 'b';
      }
      
      ArrayList<Integer> rt = occupiedSquares(bRooks);
      
      for (Integer i : rt) {
         targets[i] = 'r';
      }
      
      ArrayList<Integer> qt = occupiedSquares(bQueens);
      
      for (Integer i : qt) {
         targets[i] = 'q';
      }
      
      return targets;
   }
   
   // get pieces black can attack
   private char[] targetsBlack() {
      char[] targets = new char[64];
      Arrays.fill(targets, 'x');
      
      ArrayList<Integer> pt = occupiedSquares(wPawns);
      
      for (Integer i : pt) {
         targets[i] = 'P';
      }
      
      ArrayList<Integer> nt = occupiedSquares(wKnights);
      
      for (Integer i : nt) {
         targets[i] = 'N';
      }
      
      ArrayList<Integer> bt = occupiedSquares(wBishops);
      
      for (Integer i : bt) {
         targets[i] = 'B';
      }
      
      ArrayList<Integer> rt = occupiedSquares(wRooks);
      
      for (Integer i : rt) {
         targets[i] = 'R';
      }
      
      ArrayList<Integer> qt = occupiedSquares(wQueens);
      
      for (Integer i : qt) {
         targets[i] = 'Q';
      }
      
      return targets;
   }

   // get moves from start square
   public Move[] moves(String square) {
      ArrayList<Move> m = new ArrayList<Move>();
      Move[] totalMoves = moves();
      int s = toIndex(square);
      
      for (Move move : totalMoves) {
         if (move.getStart() == s) {
            m.add(move);
         }
      }
      
      Move[] arr = new Move[0];
      
      return m.toArray(arr);
   }
   
   private Move[] whiteMoves() {
      ArrayList<Move> m = new ArrayList<>();
      
      HashMap<Integer, MovePair> moves = new HashMap<>();
      
      long wPieces = wPawns | wKnights | wBishops | wRooks | wQueens | wKing;
      long bPieces = bPawns | bKnights | bBishops | bRooks | bQueens | bKing;
      
      long blockers = wPawns | wKnights | wBishops | wRooks | wQueens | bPieces; // blocking pieces
      
      long pushMask = UNIVERSE; // squares white can move to 
      long captureMask = UNIVERSE; // squares white can capture on
      
      long attacked = 0L; // squares attacked by black
      long attackers = 0L; // attackers of white king
      int attackersCount = 0;
      
      // set attacked squares
      
      long[] slidingAttacks = getSlidingAttacksBlack(blockers);
      long nonSlidingAttacks = getNonSlidingAttacksBlack(blockers);
      
      attacked |= slidingAttacks[0] | slidingAttacks[1] | slidingAttacks[2] | slidingAttacks[3];
      attacked |= nonSlidingAttacks;
      
      int kingSquare = BS.LSB(wKing);
      long[] kingSlides = new long[4];
      
      kingSlides[0] = diagonalMoves(kingSquare, blockers);
      kingSlides[1] = antidiagonalMoves(kingSquare, blockers);
      kingSlides[2] = fileMoves(kingSquare, blockers);
      kingSlides[3] = rankMoves(kingSquare, blockers);
      
      blockers |= wKing;
      
      // set attackers
      
      attackers |= getPawnCapturesWhite(kingSquare, bPawns);
      attackers |= getKnightMoves(kingSquare) & bKnights;
      
      if (attackers != 0) {
         pushMask = 0L;
         attackersCount ++;
      }
      
      long temp1 = kingSlides[0] & (bBishops | bQueens);
      
      if (temp1 != 0) {
         pushMask = kingSlides[0] & diagonalMoves(BS.LSB(temp1), blockers);
         attackersCount ++;
      }
      
      long temp2 = kingSlides[1] & (bBishops | bQueens);
      
      if (temp2 != 0) {
         pushMask = kingSlides[1] & antidiagonalMoves(BS.LSB(temp2), blockers);
         attackersCount ++;
      }
      
      long temp3 = kingSlides[2] & (bRooks | bQueens);
      
      if (temp3 != 0) {
         pushMask = kingSlides[2] & fileMoves(BS.LSB(temp3), blockers);
         attackersCount ++;
      }
      
      long temp4 = kingSlides[3] & (bRooks | bQueens);
      
      if (temp4 != 0) {
         pushMask = kingSlides[3] & rankMoves(BS.LSB(temp4), blockers);
         attackersCount ++;
      }
      
      attackers |= temp1 | temp2 | temp3 | temp4;
      
      /* king moves */
      
      char[] targets = targetsWhite();
      
      moves.put(kingSquare, new MovePair('K', getKingMoves(kingSquare, attacked) & ~wPieces));
      
      /* double check */
      
      if (attackersCount > 1) {
         createMoves(m, moves);
         for (Move move : m) {
            move.setCapture(targets[move.getTarget()]);
         }
         return m.toArray(new Move[0]);
      } else if (attackersCount == 1) {
         captureMask = attackers;
      }
      
      /* pseudo-legal moves */
      
      long safe = pushMask | captureMask;
      
      for (Integer square : occupiedSquares(wPawns)) {
         moves.put(square, new MovePair('P', getPawnPushesWhite(square, ~blockers) & pushMask | getPawnCapturesWhite(square, bPieces) & captureMask));
      }
      
      for (Integer square : occupiedSquares(wKnights)) {
         moves.put(square, new MovePair('N', getKnightMoves(square) & ~wPieces & safe));
      }
      
      for (Integer square : occupiedSquares(wBishops)) {
         moves.put(square, new MovePair('B', getBishopMoves(square, blockers) & ~wPieces & safe));
      }
      
      for (Integer square : occupiedSquares(wRooks)) {
         moves.put(square, new MovePair('R', getRookMoves(square, blockers) & ~wPieces & safe));
      }
      
      for (Integer square : occupiedSquares(wQueens)) {
         moves.put(square, new MovePair('Q', getQueenMoves(square, blockers) & ~wPieces & safe));
      }
      
      /* pinned pieces */
      
      if (captureMask != UNIVERSE) {
         slidingAttacks[0] &= ~pushMask;
         slidingAttacks[1] &= ~pushMask;
         slidingAttacks[2] &= ~pushMask;
         slidingAttacks[3] &= ~pushMask;
      }
      
      long intersect = kingSlides[0] & slidingAttacks[0] & wPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & DIAGONALS[square]);
      }
      
      intersect = kingSlides[1] & slidingAttacks[1] & wPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & ANTIDIAGONALS[square]);
      }
      
      intersect = kingSlides[2] & slidingAttacks[2] & wPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & FILES[square]);
      }
      
      intersect = kingSlides[3] & slidingAttacks[3] & wPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & RANKS[square]);
      }
      
      // add moves
      
      createMoves(m, moves);
      
      // set capture squares
      
      for (Move move : m) {
         move.setCapture(targets[move.getTarget()]);
      }
      
      // en passant captures
      
      long epbb = 1L << epsquare;
      
      if (epsquare >= 0 && (pushMask & epbb | captureMask & epbb >>> 8) != 0) {
         m.addAll(getEPCapturesWhite(epbb));
      }
      
      // king castles
      
      if (captureMask == UNIVERSE) {
         m.addAll(getKingCastlesWhite(blockers, attacked));
      }
      
      return m.toArray(new Move[0]);
   }
   
   private Move[] blackMoves() {
      ArrayList<Move> m = new ArrayList<>();
      
      HashMap<Integer, MovePair> moves = new HashMap<>();
      
      long wPieces = wPawns | wKnights | wBishops | wRooks | wQueens | wKing;
      long bPieces = bPawns | bKnights | bBishops | bRooks | bQueens | bKing;
      
      long blockers = wPieces | bPawns | bKnights | bBishops | bRooks | bQueens; // blocking pieces
      
      long pushMask = UNIVERSE; // squares white can move to 
      long captureMask = UNIVERSE; // squares white can capture on
      
      long attacked = 0L; // squares attacked by black
      long attackers = 0L; // attackers of white king
      int attackersCount = 0;
      
      // set attacked squares
      
      long[] slidingAttacks = getSlidingAttacksWhite(blockers);
      long nonSlidingAttacks = getNonSlidingAttacksWhite(blockers);
      
      attacked |= slidingAttacks[0] | slidingAttacks[1] | slidingAttacks[2] | slidingAttacks[3];
      attacked |= nonSlidingAttacks;
      
      int kingSquare = BS.LSB(bKing);
      long[] kingSlides = new long[4];
      
      kingSlides[0] = diagonalMoves(kingSquare, blockers);
      kingSlides[1] = antidiagonalMoves(kingSquare, blockers);
      kingSlides[2] = fileMoves(kingSquare, blockers);
      kingSlides[3] = rankMoves(kingSquare, blockers);
      
      blockers |= bKing;
      
      // set attackers
      
      attackers |= getPawnCapturesBlack(kingSquare, wPawns);
      attackers |= getKnightMoves(kingSquare) & wKnights;
      
      if (attackers != 0) {
         pushMask = 0L;
         attackersCount ++;
      }
      
      long temp1 = kingSlides[0] & (wBishops | wQueens);
      
      if (temp1 != 0) {
         pushMask = kingSlides[0] & diagonalMoves(BS.LSB(temp1), blockers);
         attackersCount ++;
      }
      
      long temp2 = kingSlides[1] & (wBishops | wQueens);
      
      if (temp2 != 0) {
         pushMask = kingSlides[1] & antidiagonalMoves(BS.LSB(temp2), blockers);
         attackersCount ++;
      }
      
      long temp3 = kingSlides[2] & (wRooks | wQueens);
      
      if (temp3 != 0) {
         pushMask = kingSlides[2] & fileMoves(BS.LSB(temp3), blockers);
         attackersCount ++;
      }
      
      long temp4 = kingSlides[3] & (wRooks | wQueens);
      
      if (temp4 != 0) {
         pushMask = kingSlides[3] & rankMoves(BS.LSB(temp4), blockers);
         attackersCount ++;
      }
      
      attackers |= temp1 | temp2 | temp3 | temp4;
      
      /* king moves */
      
      char[] targets = targetsBlack();
      
      moves.put(kingSquare, new MovePair('k', getKingMoves(kingSquare, attacked) & ~bPieces));
      
      /* double check */
      
      if (attackersCount > 1) {
         createMoves(m, moves);
         for (Move move : m) {
            move.setCapture(targets[move.getTarget()]);
         }
         return m.toArray(new Move[0]);
      } else if (attackersCount == 1) {
         captureMask = attackers;
      }
      
      /* pseudo-legal moves */
      
      long safe = pushMask | captureMask;
      
      for (Integer square : occupiedSquares(bPawns)) {
         moves.put(square, new MovePair('p', getPawnPushesBlack(square, ~blockers) & pushMask | getPawnCapturesBlack(square, wPieces) & captureMask));
      }
      
      for (Integer square : occupiedSquares(bKnights)) {
         moves.put(square, new MovePair('n', getKnightMoves(square) & ~bPieces & safe));
      }
      
      for (Integer square : occupiedSquares(bBishops)) {
         moves.put(square, new MovePair('b', getBishopMoves(square, blockers) & ~bPieces & safe));
      }
      
      for (Integer square : occupiedSquares(bRooks)) {
         moves.put(square, new MovePair('r', getRookMoves(square, blockers) & ~bPieces & safe));
      }
      
      for (Integer square : occupiedSquares(bQueens)) {
         moves.put(square, new MovePair('q', getQueenMoves(square, blockers) & ~bPieces & safe));
      }
      
      /* pinned pieces */
      
      if (captureMask != UNIVERSE) {
         slidingAttacks[0] &= ~pushMask;
         slidingAttacks[1] &= ~pushMask;
         slidingAttacks[2] &= ~pushMask;
         slidingAttacks[3] &= ~pushMask;
      }
      
      long intersect = kingSlides[0] & slidingAttacks[0] & bPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & DIAGONALS[square]);
      }
      
      intersect = kingSlides[1] & slidingAttacks[1] & bPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & ANTIDIAGONALS[square]);
      }
      
      intersect = kingSlides[2] & slidingAttacks[2] & bPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & FILES[square]);
      }
      
      intersect = kingSlides[3] & slidingAttacks[3] & bPieces;
      
      for (Integer square : occupiedSquares(intersect)) {
         long bb = moves.get(square).getMoves();
         moves.get(square).setMoves(bb & RANKS[square]);
      }
      
      // add moves
      
      createMoves(m, moves);
      
      // set capture squares
      
      for (Move move : m) {
         move.setCapture(targets[move.getTarget()]);
      }
      
      // en passant captures
      
      long epbb = 1L << epsquare;
      
      if (epsquare >= 0 && (pushMask & epbb | captureMask & epbb << 8) != 0) {
         m.addAll(getEPCapturesBlack(epbb));
      }
      
      // king castles
      
      if (captureMask == UNIVERSE) {
         m.addAll(getKingCastlesBlack(blockers, attacked));
      }
      
      return m.toArray(new Move[0]);
   }
   
   // get all moves
   public Move[] moves() {
      return turn ? whiteMoves() : blackMoves();
   }
   
   // copy  
   public Chess copy() {
      return new Chess(
         wPawns,
         bPawns, 
         wKnights, 
         bKnights, 
         wBishops, 
         bBishops, 
         wRooks, 
         bRooks, 
         wQueens, 
         bQueens, 
         wKing, 
         bKing,
         turn, 
         castle.clone(), 
         epsquare,
         movesSinceCapture,
         fullMoves
         );
   }
   
   // remove piece
   
   private void removePiece(int startSquare, char piece) {
      switch (piece) {
         case 'p':
            bPawns &= ~(1L << startSquare);
            break;
         case 'n':
            bKnights &= ~(1L << startSquare);
            break;
         case 'b':
            bBishops &= ~(1L << startSquare);
            break;
         case 'r':
            bRooks &= ~(1L << startSquare);
            break;
         case 'q':
            bQueens &= ~(1L << startSquare);
            break;
         case 'k':
            bKing &= ~(1L << startSquare);
            break;
         case 'P':
            wPawns &= ~(1L << startSquare);
            break;
         case 'N':
            wKnights &= ~(1L << startSquare);
            break;
         case 'B':
            wBishops &= ~(1L << startSquare);
            break;
         case 'R':
            wRooks &= ~(1L << startSquare);
            break;
         case 'Q':
            wQueens &= ~(1L << startSquare);
            break;
         case 'K':
            wKing &= ~(1L << startSquare);
      }
   }
   
   // add piece
   
   private void addPiece(int startSquare, char piece) {
      switch (piece) {
         case 'p':
            bPawns |= (1L << startSquare);
            break;
         case 'n':
            bKnights |= (1L << startSquare);
            break;
         case 'b':
            bBishops |= (1L << startSquare);
            break;
         case 'r':
            bRooks |= (1L << startSquare);
            break;
         case 'q':
            bQueens |= (1L << startSquare);
            break;
         case 'k':
            bKing |= (1L << startSquare);
            break;
         case 'P':
            wPawns |= (1L << startSquare);
            break;
         case 'N':
            wKnights |= (1L << startSquare);
            break;
         case 'B':
            wBishops |= (1L << startSquare);
            break;
         case 'R':
            wRooks |= (1L << startSquare);
            break;
         case 'Q':
            wQueens |= (1L << startSquare);
            break;
         case 'K':
            wKing |= (1L << startSquare);
      }
   }
      
   // apply move to chess board
   public void move(Move m) {
      int startSquare = m.getStart();
      int targetSquare = m.getTarget();
      char piece = pieceAt(startSquare);
      char promote = m.getPromote();
      char capture = m.getCapture();
      
      if (historyOn) {
         history.add(m);
         pgn.add(SAN(m));
      }
      
      removePiece(startSquare, piece);
      removePiece(targetSquare, capture);
      
      if (promote == 'x') {
         addPiece(targetSquare, piece);
      } else {
         addPiece(targetSquare, promote);
      }
      
      movesSinceCapture = capture == 'x' ? movesSinceCapture + 1 : 1;
      totalHalfMoves.add(movesSinceCapture);
      fullMoves = turn ? fullMoves : fullMoves + 1;
      
      switch (m.getCastle()) {
         case 'K':
            removePiece(7, 'R');
            addPiece(5, 'R');
            castle[0] = false;
            castle[1] = false;
            break;
         case 'Q':
            removePiece(0, 'R');
            addPiece(3, 'R');
            castle[0] = false;
            castle[1] = false;
            break;
         case 'k':
            removePiece(63, 'r');
            addPiece(61, 'r');
            castle[2] = false;
            castle[3] = false;
            break;
         case 'q':
            removePiece(56, 'r');
            addPiece(59, 'r');
            castle[2] = false;
            castle[3] = false;
            break;
         default : 
            if (piece == 'K') {
               castle[0] = false;
               castle[1] = false;
            } else if (piece == 'k') {
               castle[2] = false;
               castle[3] = false;
            }
            
            if (startSquare == 7 || targetSquare == 7) {
               castle[0] = false;
            } else if (startSquare == 0 || targetSquare == 0) {
               castle[1] = false;
            }
            
            if (startSquare == 63 || targetSquare == 63) {
               castle[2] = false;
            } else if (startSquare == 56 || targetSquare == 56) {
               castle[3] = false;
            }
      }
      
      totalCastles.add(new Boolean[]{castle[0], castle[1], castle[2], castle[3]});
      
      if (m.getEnPassant()) {
         if (turn) {
            removePiece(targetSquare - 8, 'p');
         } else {
            removePiece(targetSquare + 8, 'P');
         }
      }
      
      epsquare = -1;
      
      if (m.getDoublePawnPush()) {
         if (turn) {
            epsquare = targetSquare - 8;
         } else {
            epsquare = targetSquare + 8;
         }
      }
      
      totalEpSquares.add(epsquare);
      
      turn = !turn;
   }
   
   // undo and return last half move
   public Move undo() {
      if (history.size() == 0) {
         return null;
      }
      
      Move m = history.remove(history.size() - 1);
      int target = m.getTarget();
      char piece = pieceAt(target);
      
      turn = !turn;
      pgn.remove(history.size());
      
      removePiece(target, piece);
      
      if (m.getPromote() == 'x') {
         addPiece(m.getStart(), piece);
      } else {
         addPiece(m.getStart(), turn ? 'P' : 'p');
      }
      
      switch (m.getCastle()) {
         case 'K':
            removePiece(5, 'R');
            addPiece(7, 'R');
            break;
         case 'Q':
            removePiece(3, 'R');
            addPiece(0, 'R');
            break;
         case 'k':
            removePiece(61, 'r');
            addPiece(63, 'r');
            break;
         case 'q':
            removePiece(59, 'r');
            addPiece(56, 'r');
      }
      
      if (m.getEnPassant()) {
         if (turn) {
            addPiece(target - 8, m.getCapture());
         } else {
            addPiece(target + 8, m.getCapture());
         }
      } else {
         addPiece(target, m.getCapture());
      }
      
      fullMoves -= turn ? 0 : 1;
      
      if (history.size() > 0) {
         totalCastles.remove(history.size());
         Boolean[] tc = totalCastles.get(history.size() - 1);      
         castle[0] = tc[0];
         castle[1] = tc[1];
         castle[2] = tc[2];
         castle[3] = tc[3];
         totalHalfMoves.remove(history.size());
         movesSinceCapture = totalHalfMoves.get(history.size() - 1);
         totalEpSquares.remove(history.size());
         epsquare = totalEpSquares.get(history.size() - 1);
      } else {
         totalHalfMoves.remove(0);
         movesSinceCapture = 0;
         totalEpSquares.remove(0);
         epsquare = -1;
         totalCastles.remove(0);
      }
      
      return m;
   }
   
   private boolean inCheckWhite(int k, long b) {
      if ((getBishopMoves(k, b) & (bBishops | bQueens)) != 0) {
         return true;
      }
      
      if ((getRookMoves(k, b) & (bRooks | bQueens)) != 0) {
         return true;
      }
      
      if ((getKnightMoves(k) & bKnights) != 0) {
         return true;
      }
      
      if (getPawnCapturesWhite(k, bPawns) != 0) {
         return true;
      }
      
      return false;
   }
   
   private boolean inCheckBlack(int k, long b) {
      if ((getBishopMoves(k, b) & (wBishops | wQueens)) != 0) {
         return true;
      }
      
      if ((getRookMoves(k, b) & (wRooks | wQueens)) != 0) {
         return true;
      }
      
      if ((getKnightMoves(k) & wKnights) != 0) {
         return true;
      }
      
      if (getPawnCapturesBlack(k, wPawns) != 0) {
         return true;
      }
      
      return false;
   }
   
   // returns turn ('w' or 'b')
   public char getTurn() {
      return turn ? 'w' : 'b';
   }
   
   // returns if the king is in check
   public boolean inCheck() { 
      long blockers = wPawns | wKnights | wBishops | wRooks | wQueens | wKing | bPawns | bKnights | bBishops | bRooks | bQueens | bKing;
      
      if (turn) {
         return inCheckWhite(BS.LSB(wKing), blockers);
      } else {
         return inCheckBlack(BS.LSB(bKing), blockers);
      }
   }
   
   // returns if position is checkmate
   public boolean inCheckmate() {
      return inCheck() && moves().length == 0;
   }
   
   // returns if position is draw
   public boolean inDraw() {
      return inStalemate() || fiftyMoves() || insufficientMaterial();
   }
   
   // returns if draw by stalemate
   public boolean inStalemate() {
      return !inCheck() && moves().length == 0;
   }
   // returns if draw by insufficient material
   public boolean insufficientMaterial() {
      if ((wPawns | wRooks | wQueens | bPawns | bRooks | bQueens) != 0) {
         return false;
      }
      
      int wN = Long.bitCount(wKnights);
      int bN = Long.bitCount(bKnights);
      int wB = Long.bitCount(wBishops);
      int bB = Long.bitCount(bBishops);
      
      if (wN + bN + wB + bB <= 1) {
         return true; 
      } else if (wN + bN == 0 && ((LIGHTSQUARES & wBishops) == 0) == ((LIGHTSQUARES & bBishops) == 0)) {
         return true;    
      }
      
      return false;
   }
   
   // returns if draw by fifty move rule
   public boolean fiftyMoves() {
      return movesSinceCapture >= 100;
   }
   
   public static String toSquare(int index) {
      return "" + (char) (97 + index % 8) + (index / 8 + 1);
   }
   
   public static int toIndex(String square) {
      return (Character.getNumericValue(square.charAt(1)) - 1) * 8 + square.charAt(0) - 'a';
   }
   
   // move to pgn
   public String SAN(Move m) {
      String code = "";
      char piece = Character.toUpperCase(pieceAt(m.getStart()));
      
      // castling
      if (m.getCastle() != 'n') {
         if (Character.toLowerCase(m.getCastle()) == 'k') {
            return "O-O";
         } else {
            return "O-O-O";
         }
      }
      
      if (piece != 'P') {
         code += piece;
      }
      
      Move[] mvs = moves();
      
      boolean fileMatch = false;
      boolean rankMatch = false;
      boolean targetMatch = false;
      
      for (int i = 0; i < mvs.length; i ++) {
         if (mvs[i].getStart() != m.getStart() && mvs[i].getTarget() == m.getTarget() && Character.toUpperCase(pieceAt(mvs[i].getStart())) == piece) {
            if (mvs[i].getStart() % 8 == m.getStart() % 8) {
               fileMatch = true;
            } else if (mvs[i].getStart() / 8 == m.getStart() / 8) {
               rankMatch = true;
            }
            
            targetMatch = true;
         }
      }
      
      if (targetMatch || piece == 'P' && m.getCapture() != 'x') {
         if (fileMatch && rankMatch) {
            code += toSquare(m.getStart());
         } else if (fileMatch) {
            code += toSquare(m.getStart()).charAt(1);
         } else {
            code += toSquare(m.getStart()).charAt(0);
         }
      }
      
      if (m.getCapture() != 'x') {
         code += 'x';
      }
      
      code += toSquare(m.getTarget());
      
      if (m.getPromote() != 'x') {
         code += "=" + Character.toUpperCase(m.getPromote());
      }
      
      Chess c = copy();
      c.move(m);
      
      if (c.inCheckmate()) {
         code += "#";
      } else if (c.inCheck()) {
         code += "+";
      }
      
      return code;
   }
   
   // return pgn
   public String toPGN() {
      String code = "";
      
      for (int i = 0; i < pgn.size(); i ++) {
         if (i % 2 == 0) {
            code += (i / 2 + 1) + ". ";
         }
         
         code += pgn.get(i) + " ";
      }
      
      if (inCheckmate()) {
         if (turn) {
            code += "0-1";
         } else {
            code += "1-0";
         }
      } else if (inDraw()) {
         code += "1/2-1/2"; 
      }
      
      return code;
   }
   
   // set board with FEN
   public void set(String fen) {
      int index = 0;
      int square = 56; 
      
      clear();
      
      while (fen.charAt(index) != ' ') {
         switch (fen.charAt(index)) {
            case 'P': 
               wPawns |= 1L << square++;
               break;
            case 'p':
               bPawns |= 1L << square ++;
               break;
            case 'N': 
               wKnights |= 1L << square ++;
               break;
            case 'n':
               bKnights |= 1L << square ++;
               break;
            case 'B':
               wBishops |= 1L << square ++;
               break;
            case 'b': 
               bBishops |= 1L << square ++;
               break;
            case 'R': 
               wRooks |= 1L << square ++;
               break;
            case 'r':
               bRooks |= 1L << square ++;
               break;
            case 'Q': 
               wQueens |= 1L << square ++;
               break;
            case 'q':
               bQueens |= 1L << square ++;
               break;
            case 'K': 
               wKing |= 1L << square ++;
               break;
            case 'k':
               bKing |= 1L << square ++;
               break;
            case '1':
               square ++;
               break;
            case '2':
               square += 2;
               break;
            case '3':
               square += 3;
               break;
            case '4':
               square += 4;
               break;
            case '5':
               square += 5;
               break;
            case '6':
               square += 6;
               break;
            case '7':
               square += 7;
               break;
            case '8':
               square += 8;
               break;
            case '/':
               square -= 16;
         }
         
         index ++;
      }
      
      turn = fen.charAt(++index) == 'w';
      
      index += 2;
      
      castle = new boolean[4];
      
      while (fen.charAt(index) != ' ') {
         switch (fen.charAt(index)) {
            case 'K': 
               castle[0] = true;
               break;
            case 'Q':
               castle[1] = true;
               break;
            case 'k': 
               castle[2] = true;
               break;
            case 'q':
               castle[3] = true;
               break;
         }
         
         index ++;
      }
      
      if (fen.charAt(++index) == '-') {
         epsquare = -1;
         index += 2;
      } else {
         epsquare = toIndex(fen.substring(index, index + 2));
         index += 3;
      }
      
      String num = "";
      
      while (fen.charAt(index) != ' ') {
         num += fen.charAt(index);
         index ++;
      }
      
      movesSinceCapture = Integer.parseInt(num);
      
      index ++;
      
      fullMoves = Integer.parseInt(fen.substring(index));
   }
   
   // get FEN string
   public String toFEN() {
      String fen = "";
      int empty = 0;
      
      // piece positions
      
      for (int i = 7; i >= 0; i --) {
         for (int j = 0; j < 8; j ++) {
            int square = i * 8  + j;
            
            char piece = pieceAt(square);
            
            if (piece == 'x') {
               empty ++;
            } else {
               fen += empty > 0 ? "" + empty + piece : "" + piece;
               empty = 0;
            }
         }
         
         if (empty > 0) {
            fen += empty;
            empty = 0;
         }
         
         if (i > 0) {
            fen += "/";
         }
      }
      
      // turn
      
      fen += turn ? " w " : " b ";
      
      // castling rights
      
      String castling = "";
      
      if (castle[0]) {
         castling += "K";
      } 
      
      if (castle[1]) {
         castling += "Q";
      } 
      
      if (castle[2]) {
         castling += "k";
      } 
      
      if (castle[3]) {
         castling += "q";
      }
      
      if (castling.length() == 0) {
         castling += "-";
      }
      
      fen += castling + " ";
      
      // ep target square
      
      fen += epsquare == -1 ? "-" : toSquare(epsquare);
      
      // 50 move rule
      
      fen += " " + movesSinceCapture + " " + fullMoves;
      
      return fen;
   }
   
   // get map of pieces to squares
   public ArrayList<PieceSquare> board() {
      ArrayList<PieceSquare> b = new ArrayList<>();
      
      ArrayList<Integer> s;
      
      s = occupiedSquares(bPawns);
      for (int i : s) {
         b.add(new PieceSquare('p', i));
      }
      
      s = occupiedSquares(wPawns);
      for (int i : s) {
         b.add(new PieceSquare('P', i));
      }
      
      s = occupiedSquares(bKnights);
      for (int i : s) {
         b.add(new PieceSquare('n', i));
      }
      
      s = occupiedSquares(wKnights);
      for (int i : s) {
         b.add(new PieceSquare('N', i));
      }
      
      s = occupiedSquares(bBishops);
      for (int i : s) {
         b.add(new PieceSquare('b', i));
      }
      
      s = occupiedSquares(wBishops);
      for (int i : s) {
         b.add(new PieceSquare('B', i));
      }
      
      s = occupiedSquares(bRooks);
      for (int i : s) {
         b.add(new PieceSquare('r', i));
      }
      
      s = occupiedSquares(wRooks);
      for (int i : s) {
         b.add(new PieceSquare('R', i));
      }
      
      s = occupiedSquares(bQueens);
      for (int i : s) {
         b.add(new PieceSquare('q', i));
      }
      
      s = occupiedSquares(wQueens);
      for (int i : s) {
         b.add(new PieceSquare('Q', i));
      }
      
      b.add(new PieceSquare('k', BS.LSB(bKing)));
      b.add(new PieceSquare('K', BS.LSB(wKing)));
      
      return b;
   }
   
   // perft debugging
   public long perft(int n) {
      long num = 0;
      
      if (n == 1) {
         return moves().length;
      } else if (n == 0) {
         return 1L;
      }
      
      for (Move m : moves()) {
         Chess c = copy();
         c.move(m);
         num += c.perft(n - 1);
      }
      
      return num;
   }
   
   public String perftDivide(int n) {
      String divide = "";
      
      for (Move m : moves()) {
         Chess c = copy();
         c.move(m);
         divide += toSquare(m.getStart()) + toSquare(m.getTarget()) + " " + c.perft(n - 1) + "\n";
      }
      
      return divide;
   }
   
   @Override
   public String toString() {
      String board = "";
      
      for (int i = 7; i >= 0; i --) {
         for (int j = 0; j < 8; j ++) {
            int square = i * 8  + j;
            char piece = pieceAt(square);
            
            if (piece == 'x') {
               board += "·";
            } else {
               board += piece;
            }
         }
         
         board += "\n";
      }
      
      return board;
   }
}


