package rushhour;

import java.util.*;
import java.io.*;

public class Solver {
    private Board currentBoard;
    private LinkedList<Board> boardQueue;
//    private PriorityQueue<Board> boardQueue;
//    private Comparator<Board> boardComparator;
    private HashSet<Integer> seenBoards;

    /**
     * comparator for priority queue
     * for use with heuristic cost of boards
     */
    private class BoardComparator implements Comparator<Board> {
        @Override
        public int compare(Board x, Board y) {
            return (int)(x.getHeurCost() - y.getHeurCost());    // returns int difference closest to 0
        }
    }

    /**
     * constructor for stack of boards
     * @param filename
     */
    public Solver(String filename) {
        currentBoard = new Board(filename);
        boardQueue = new LinkedList<Board>();
//        boardComparator = new BoardComparator();
//        boardQueue = new PriorityQueue<Board>(boardComparator);
        boardQueue.addLast(currentBoard);
//        boardQueue.add(currentBoard);
        seenBoards = new HashSet<Integer>();
        seenBoards.add(currentBoard.getHashNum());
    }

    /**
     * check if xCar is free to reach goal
     * @return true if there is a free path, false otherwise
     */
    private boolean canSolve() {
        Car xCar = this.currentBoard.getCar('X');
        int searchY = xCar.getY();

        for (int i = xCar.getX() + xCar.getLength(); i < 6; i++) {
            if (this.currentBoard.getCharBoard()[searchY][i] != '.')
                return false;
        }
        return true;
    }

    /**
     * check if xCar is already at goal
     * @return true if so, false otherwise
     */
    private boolean isSolved() {
        Car xCar = this.currentBoard.getCar('X');
        return xCar.getX() == 4;
    }

    private void findAllNext() {
        // make all possible moves w/ all cars, add to stack
        for (Car c : this.currentBoard.getListOfCars()) {
            Board tempFor = new Board(this.currentBoard);      // deep copy of board
            Car carCpyFor = tempFor.getCar(c.getName());       // car in currentBoard as same car in temp

            tempFor = tempFor.attemptMove(carCpyFor, Board.FORWARD);        // attempt move forwards
            if (tempFor != null && !(this.seenBoards.contains(tempFor.getHashNum()))) {
                this.boardQueue.addLast(tempFor);
//                this.boardQueue.add(tempFor);
                this.seenBoards.add(tempFor.getHashNum());
            }

            Board tempBack = new Board(this.currentBoard);                  // recreate deep copy
            Car carCpyBack = tempBack.getCar(c.getName());
            tempBack = tempBack.attemptMove(carCpyBack, Board.BACKWARD);    // attempt move backwards
            if (tempBack != null && !(this.seenBoards.contains(tempBack.getHashNum()))) {
                this.boardQueue.addLast(tempBack);
//                this.boardQueue.add(tempBack);
                this.seenBoards.add(tempBack.getHashNum());
            }
        }
    }

    public static void solveFromFile(String input, String output) {
        Solver solution = new Solver(input);

        /**
        System.out.println("INITIAL BOARD:");
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                System.out.printf(solution.currentBoard.getCharBoard()[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
         */

        while (!solution.canSolve() && !(solution.boardQueue.isEmpty())) {
            // remove board from queue
            solution.currentBoard = solution.boardQueue.removeFirst();
            // remove board from priority queue
//            solution.currentBoard = solution.boardQueue.remove();
            solution.findAllNext();
        }

        if (!solution.canSolve()) {
            // try a second time using last result
            solution.boardQueue = new LinkedList<Board>();
            solution.boardQueue.addLast(solution.currentBoard);
            solution.seenBoards = new HashSet<Integer>();
            solution.seenBoards.add(solution.currentBoard.getHashNum());

            while (!solution.canSolve() && !(solution.boardQueue.isEmpty())) {
                // remove board from queue
                solution.currentBoard = solution.boardQueue.removeFirst();
                // remove board from priority queue
//            solution.currentBoard = solution.boardQueue.remove();
                solution.findAllNext();
            }

            if (!solution.canSolve()) {
                System.out.println("cannot find solution for " + input);
                System.out.println("UNFINISHED BOARD for " + input + ":");
                for (int i = 0; i < Board.SIZE; i++) {
                    for (int j = 0; j < Board.SIZE; j++) {
                        System.out.printf(solution.currentBoard.getCharBoard()[i][j] + " ");
                    }
                    System.out.println();
                }
            }
            else {
                solution.finishSolve(input, output);
            }
        }
        else {
            solution.finishSolve(input, output);
        }
    }

    public void finishSolve(String input, String output) {
        /**
         * xCar must have free path,
         * move the car over to the goal
         */
        while (!this.isSolved()) {
            Car xCar = this.currentBoard.getCar('X');
            this.currentBoard = this.currentBoard.attemptMove(xCar, Board.FORWARD);
        }

        /**
         * take currentBoard, output list of moves to get there
         * write to output file
         */
        try {
            File fout = new File(output);
            PrintWriter boardWriter = new PrintWriter(fout);
            Board.printMovesToFile(boardWriter, this.currentBoard.getLastMoveNode());
            boardWriter.flush();
            boardWriter.close();
        } catch (Exception e) {
            System.out.println("file writing error");
        }


        System.out.println("FINAL BOARD for " + input + ":");
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                System.out.printf(this.currentBoard.getCharBoard()[i][j] + " ");
            }
            System.out.println();
        }

    }
}
