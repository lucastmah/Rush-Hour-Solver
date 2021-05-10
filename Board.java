package rushhour;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Board {
    private char[][] charBoard;
    private ArrayList<Car> listOfCars;
    private ArrayList<Character> listOfCarNames;
    private MoveNode lastMoveNode;
    private double heurCost;
    private int hashNum;

    // intended move directions
    public static final int BACKWARD = -1;
    public static final int FORWARD = 1;

    // side length of board
    public static final int SIZE = 6;

    /**
     * default constructor w/ path to file
     * @param filename
     */
    public Board(String filename) {
        lastMoveNode = null;
        listOfCars = new ArrayList<Car>();
        listOfCarNames = new ArrayList<Character>();
        try {
            /**
             * read through file
             */
            File boardFile = new File(filename);
            Scanner boardScan = new Scanner(boardFile);
            charBoard = new char[SIZE][SIZE];

            for (int i = 0; i < SIZE; i++) {
                /* if under 6 lines, NoSuchElementException */
                String line = boardScan.nextLine();
                for (int j = 0; j < SIZE; j++) {
                    /* if under length 6, IndexOutOfBoundsException */
                    charBoard[i][j] = line.charAt(j);
                    // take names of all cars
                    if (line.charAt(j) != '.' && !(listOfCarNames.contains(line.charAt(j)))) {
                        listOfCarNames.add(line.charAt(j));
                    }
                }
            }

            /**
             * find and create new cars
             * iterate through names found
             */
            for (char ch : listOfCarNames) {
                int carLen = 0;
                boolean topFound = false;    // checks if first part of car is found
                int findX = 0;
                int findY = 0;
                int findDir = 0;

                // iterate through the board
                for (int i = 0; i < SIZE; i++) {
                    for (int j = 0; j < SIZE; j++) {
                        // if char on board matches with car name ch
                        if (charBoard[i][j] == ch) {
                            carLen++;
                            // if this is first instance of car part
                            if (!topFound) {
                                findX = j;
                                findY = i;
                                topFound = true;
                            }

                            // establish direction using 2nd part of car
                            if (carLen >= 2 && j == findX) {
                                findDir = Car.VERT;
                            } else if (carLen >= 2 && i == findY) {
                                findDir = Car.HORI;
                            }
                        }
                    }
                }
                // car was never found
                if (!topFound || carLen == 0)
                    throw new Exception("could not find car");

                // make new Car with newly-found parameters
                Car foundCar = new Car(ch, findX, findY, carLen, findDir);
                if (listOfCars.contains(foundCar))     // duplicate car check
                    throw new IllegalArgumentException("duplicate car found");
                else
                    listOfCars.add(foundCar);
            }
            boardScan.close();
        } catch (FileNotFoundException e) {
            System.out.println("file error");
        } catch (NoSuchElementException e) {
            // not enough lines from scanner
            System.out.println("invalid board: not long enough");
        } catch (IndexOutOfBoundsException e) {
            // lines not long enough to scan chars
            System.out.println("invalid board: not wide enough");
        } catch (IllegalArgumentException e) {
            System.out.println("car scanning error");
        } catch (Exception e) {
            System.out.println("board scanning error");
        }

        // set heuristic cost
        heurCost = computeCost();
        // hash board of chars
        hashNum = java.util.Arrays.deepHashCode(charBoard);
    }

    /**
     * deep copy constructor
     * @param otherBoard
     */
    public Board(Board otherBoard) {
        charBoard = new char[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++)
                charBoard[i][j] = otherBoard.charBoard[i][j];
        }
        listOfCars = new ArrayList<Car>();
        for (Car c : otherBoard.listOfCars)
            listOfCars.add(new Car(c));     // deep copy of cars
        listOfCarNames = new ArrayList<Character>();
        for (char ch : otherBoard.listOfCarNames)
            listOfCarNames.add(ch);

        lastMoveNode = otherBoard.lastMoveNode;
        hashNum = otherBoard.hashNum;
    }

    // getters
    public char[][] getCharBoard() { return charBoard; }
    public ArrayList<Car> getListOfCars() { return listOfCars; }
    public Car getCar(int index) { return listOfCars.get(index); }
    public Car getCar(char lookFor) { return listOfCars.get(listOfCarNames.indexOf(lookFor)); }
    public ArrayList<Character> getListOfCarNames() { return listOfCarNames; }
    public char getCarName(int index) { return listOfCarNames.get(index); }
    public MoveNode getLastMoveNode() { return lastMoveNode; }
    public double getHeurCost() { return heurCost; }
    public int getHashNum() { return hashNum; }



    /**
     * helper fcn for attemptMove
     * takes a board, moves a car c in direction dir
     * move is assumed to be legal
     * @param c: car we want to move
     * @param dir: backward or forward (-/+ 1)
     * @return
     */
    private void moveCar(Car c, int dir) {
        if (c.getDir() == Car.VERT) {
            if (dir == FORWARD) {
                this.charBoard[c.getY()][c.getX()] = '.';
                this.charBoard[c.getY() + c.getLength()][c.getX()] = c.getName();
            }
            else if (dir == BACKWARD) {
                this.charBoard[c.getY() - 1][c.getX()] = c.getName();
                this.charBoard[c.getY() + c.getLength() - 1][c.getX()] = '.';
            }
            c.setY(c.getY() + dir);
        }
        else if (c.getDir() == Car.HORI) {
            if (dir == FORWARD) {
                this.charBoard[c.getY()][c.getX()] = '.';
                this.charBoard[c.getY()][c.getX() + c.getLength()] = c.getName();
            }
            else if (dir == BACKWARD) {
                this.charBoard[c.getY()][c.getX() - 1] = c.getName();
                this.charBoard[c.getY()][c.getX() + c.getLength() - 1] = '.';
            }
            c.setX(c.getX() + dir);
        }

        this.heurCost = computeCost();      // update heuristic cost
        this.hashNum = java.util.Arrays.deepHashCode(this.charBoard);   // update hashcode of board
    }

    /**
     * check if car can move by 1 in direction dir
     * is move legal?
     * @param c: Car we want to move
     * @param dir: direction of move (forward/back, +/- 1)
     * @return new board w/ legal move, otherwise null if illegal move
     */
    public Board attemptMove(Car c, int dir) {
        /**
         * check if car can move by 1 in direction dir
         * if yes, return new board
         * else, return null
         */
        if (dir == FORWARD) {
            // forwards HORIZONTAL
            if (c.getDir() == Car.HORI && c.getX() + c.getLength() < 6) {
                if (this.charBoard[c.getY()][c.getX()+c.getLength()] == '.') {
                    moveCar(c, dir);
                    this.lastMoveNode = new MoveNode(dir, c, this.lastMoveNode);
                    return this;
                }
            }
            // forwards VERTICAL
            else if (c.getDir() == Car.VERT && c.getY() + c.getLength() < 6) {
                if (this.charBoard[c.getY()+c.getLength()][c.getX()] == '.') {
                    moveCar(c, dir);
                    this.lastMoveNode = new MoveNode(dir, c, this.lastMoveNode);
                    return this;
                }
            }
        }
        else if (dir == BACKWARD) {
            // backwards HORIZONTAL
            if (c.getDir() == Car.HORI && c.getX() + dir >= 0) {
                if (this.charBoard[c.getY()][c.getX()+dir] == '.') {
                    moveCar(c, dir);
                    this.lastMoveNode = new MoveNode(dir, c, this.lastMoveNode);
                    return this;
                }
            }
            // backwards VERTICAL
            else if (c.getDir() == Car.VERT && c.getY() + dir >= 0) {
                if (this.charBoard[c.getY()+dir][c.getX()] == '.') {
                    moveCar(c, dir);
                    this.lastMoveNode = new MoveNode(dir, c, this.lastMoveNode);
                    return this;
                }
            }
        }

        // thus, move is illegal
        return null;
    }

    /**
     * calculates heuristic cost for priority queue
     * specifically, counts number of cars that block X car
     */
    public double computeCost() {
        int blockCost = 0;
        int moveCost = 0;
        double closeCost = 0;
        int exitCost = 0;
        Car xCar = this.getCar('X');


        for (Car c : this.listOfCars) {
            /**
             * implementing blockCost:
             * how many cars block xCar's path?
             * NOTE: car cannot be horizontal and on same row as xCar
             * otherwise, there would be no solution
             * thus, any car blocking xCar must be vertical
             */
            boolean block = false;
            if (c.getDir() == Car.VERT) {
                // check that c is blocking xCar somehow
                for (int i = 0; i < c.getLength(); i++) {
                    if (c.getY() + i == xCar.getY()) {
                        blockCost++;
                        block = true;
                    }
                }
//                if (block) {
//                    // recursively check what cars block c
//                    computeCostHelper(c, blockCost);
//                }
            }

            /**
             * implementing moveCost:
             * how many possible moves are there in the board's current state?
             * NOTE: check spaces in front + behind car
             * conditions modified from attemptMove()
             */
            // forwards HORIZONTAL
            if (c.getDir() == Car.HORI && c.getX() + c.getLength() < 6) {
                if (this.charBoard[c.getY()][c.getX()+c.getLength()] == '.')
                    moveCost++;
            }
            // forwards VERTICAL
            else if (c.getDir() == Car.VERT && c.getY() + c.getLength() < 6) {
                if (this.charBoard[c.getY()+c.getLength()][c.getX()] == '.')
                    moveCost++;
            }
            // backwards HORIZONTAL
            if (c.getDir() == Car.HORI && c.getX() > 0) {
                if (this.charBoard[c.getY()][c.getX()-1] == '.')
                    moveCost++;
            }
            // backwards VERTICAL
            else if (c.getDir() == Car.VERT && c.getY() > 0) {
                if (this.charBoard[c.getY()-1][c.getX()] == '.')
                    moveCost++;
            }

            /**
             * implementing closeCost:
             * how close are cars to the exit?
             * NOTE: for each car that isn't xCar, return distance from car to exit
             * using pythagorean theorem
             * if a car that isn't xCar is close to exit,
             * creates higher cost than car that is further from exit
             */
            int a = java.lang.Math.abs(6 - c.getX());
            int b = java.lang.Math.abs(2 - c.getY());
            closeCost += java.lang.Math.sqrt(a*a + b*b);        // sqrt( a^2 + b^2 )
        }

        /**
         * implementing exitCost:
         * how much space is there near the exit?
         * NOTE: if spaces near the exit have car parts, cost is increased
         */
        for (int i = 0; i < Board.SIZE; i++) {
            // check column in front of exit
            if (this.charBoard[i][5] != '.') exitCost++;
            // check row of xCar, avoid counting duplicate square in front of exit
            if (this.charBoard[2][i] != '.' && i != 5)
                exitCost += i;
        }

        return blockCost + moveCost - closeCost + exitCost;
    }

//    public void computeCostHelper(Car c, int cost) {
//        /**
//         * check for other cars along the path of c
//         */
//        if (c.getDir() == Car.HORI) {
//
//        }
//    }

    public static void printMovesToScreen(MoveNode currNode) {
        if (currNode.getParentMove() != null)
            printMovesToScreen(currNode.getParentMove());
        System.out.println(currNode.getMoveName());
    }

    public static void printMovesToFile(PrintWriter writer, MoveNode currNode) {
        if (currNode.getParentMove() != null)
            printMovesToFile(writer, currNode.getParentMove());
        writer.append(currNode.getMoveName() + "\n");
    }
}
