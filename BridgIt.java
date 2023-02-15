import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


// class representing constants 
class GameConstants {
  GameConstants() {}

  // colors on the board
  Color player1Color = Color.CYAN;
  Color player2Color = Color.BLUE;
  Color mtColor = Color.WHITE;

  // size of cell
  int cellSize = 100;

}

// class representing a cell with access to its color and neighbors
class Cell {
  Color color;
  Cell above;
  Cell below;
  Cell right;
  Cell left;
  Posn posn;

  // convenience constructor
  Cell(Color color, Cell above, Cell below, Cell right, Cell left, Posn posn) {
    this.color = color;
    this.above = above;
    this.below = below;
    this.right = right;
    this.left = left;
    this.posn = posn;
  }

  // draw this cell as a square with dimensions and a color
  WorldImage draw() {
    return new RectangleImage(new GameConstants().cellSize, 
        new GameConstants().cellSize, OutlineMode.SOLID, this.color);

  }

  // set each cell so it's neighbors are in the correct locations when
  // game first starts (initial conditions)
  void setLocation(Cell above, Cell below, Cell right, Cell left) {
    this.above = above;
    this.below = below;
    this.right = right;
    this.left = left;
  }


  //checks if neighbors are the same color from left to right
  boolean checkHorizontalHelp(Cell from, Color color, ICollection<Cell> worklist) {
    if (this.color != color) {
      return false;
    }

    Stack<Cell> alreadySeen = new Stack<Cell>();

    // Initialize the worklist with the from vertex
    worklist.add(from);

    // Works as long as work list is not empty
    while (!worklist.isEmpty()) {
      Cell next = worklist.remove();

      // if right is null this means cells made it from left
      // to right without stopping so there exists a path
      if (next.right == null) {
        return true; 
      }

      // checks all neighbors for potential paths
      else {
        if (next.above != null && next.above.color == color) {
          worklist.add(next.above);
        }
        if (next.below != null && next.below.color == color) {
          worklist.add(next.below);
        }
        if (next.right != null && next.right.color == color) {
          worklist.add(next.right);
        }
        if (next.left != null && next.left.color == color) {
          worklist.add(next.left);
        }

        // add next to alreadySeen since its no longer necessary
        alreadySeen.add(next);
      }
    }
    // return false if there's no found vertex and none left to try
    return false;
  }


  //checks if neighbors are the same color from top to bottom
  boolean checkVerticalHelp(Cell from, Color color, ICollection<Cell> worklist) {
    if (this.color != color) {
      return false;
    }

    Queue<Cell> alreadySeen = new Queue<Cell>();

    // Initialize the worklist with the from vertex
    worklist.add(from);

    // Works as long as work list is not empty
    while (!worklist.isEmpty()) {
      Cell next = worklist.remove();

      // if below is null this means cells made it from top
      // to bottom without stopping so there exists a path
      if (next.below == null) {
        return true; 
      }

      else if (alreadySeen.contents.contains(next)) {
        // do nothing
      }

      // checks all neighbors for potential paths
      else {
        if (next.above != null && next.above.color == color) {
          worklist.add(next.above);
        }
        if (next.below != null && next.below.color == color) {
          worklist.add(next.below);
        }
        if (next.right != null && next.right.color == color) {
          worklist.add(next.right);
        }
        if (next.left != null && next.left.color == color) {
          worklist.add(next.left);
        }

        // add next to alreadySeen since its no longer necessary
        alreadySeen.add(next);
      }
    }

    // return false if there's no found vertex and none left to try
    return false;

  }

}


// class representing BridgIt game
class BridgIt extends World {
  ArrayList<ArrayList<Cell>> board;
  boolean turn;
  int boardSize;

  // convenience constructor
  BridgIt(ArrayList<ArrayList<Cell>> board, boolean turn, int boardSize) {
    this.board = board;
    this.turn = true;
    this.boardSize = boardSize;

  }

  // constructor for boardSize
  BridgIt(int boardSize) {
    if (boardSize % 2 == 0 || boardSize < 3) {
      throw new IllegalArgumentException("board size must be odd and greater than 3");
    }
    this.board = new ArrayList<ArrayList<Cell>>();
    this.turn = true;
    this.boardSize = 5;

    // calls method initializing game in constructor
    // also link cells together
    this.initializeGame();

  }

  // constructor that initializes cells on board
  BridgIt() {
    this.board = new ArrayList<ArrayList<Cell>>();
    this.turn = true;
    this.boardSize = 5;

    // calls method initializing game in constructor
    // also link cells together
    this.initializeGame();

  }

  // method that builds board and initializes cells
  void initializeGame() {

    // builds board horizontally
    for (int x = 0; x < this.boardSize; x++) {

      // creates columns (vertical array lists of cells)
      ArrayList<Cell> column = new ArrayList<Cell>();

      // builds board vertically
      for (int y = 0; y < this.boardSize; y++) {

        // what to do x is an even number
        if (x % 2 == 0) {

          // if x is even and y is odd create a cell with player1 color
          if (y % 2 == 1) {
            column.add(new Cell(new GameConstants().player1Color, null, null, 
                null, null, new Posn(y, x)));
          }

          // if x is even and y is even create a cell with empty cell color
          else {
            column.add(new Cell(new GameConstants().mtColor, null, null, null, 
                null, new Posn(y, x)));
          }
        }

        // what to do x is an odd number
        else {

          // if x is odd and y is even create a cell with player2 cell color
          if (y % 2 == 0) {
            column.add(new Cell(new GameConstants().player2Color, null, null, 
                null, null, new Posn(y, x)));
          }

          // if x is odd and y is odd create a cell with mt color
          else {
            column.add(new Cell(new GameConstants().mtColor, null, null, null, 
                null, new Posn(y, x)));
          }
        }
      }

      // creates respective column at each x value
      this.board.add(column);
    }

    // updates locations as game is being constructed
    this.updateLocation();
  }

  // updates location of cells and cells around it after initializing
  void updateLocation() {

    // traverses array list horizontally
    for (int x = 0; x < this.board.get(1).size() - 1; x++) {
      Cell above = null;
      Cell below = null;
      Cell left = null;
      Cell right = null;

      // traverses array list vertically
      for (int y = 0; y < this.board.get(1).size() - 1; y++) {

        // if y is above 0 (within the board) update location so that it 
        // accounts for the y value of the above row
        if (y > 0) {
          above = this.board.get(y - 1).get(x);
        }

        // if y is below board size (within the board) update location so that it 
        // accounts for the y value of the below row
        if (y < this.board.get(1).size()) {
          below = this.board.get(y + 1).get(x);
        }

        // if x is above 0 (within the board) update location so that it 
        // accounts for the x value of the previous row
        if (x > 0) {
          left = this.board.get(y).get(x - 1);
        }

        // if x is below board size (within the board) update location so that it 
        // accounts for the x value of the next row
        if (x < this.board.get(1).size()) {
          right = this.board.get(y).get(x + 1);
        }

        // set location with new updated locations
        this.board.get(y).get(x).setLocation(above, below, left, right);
      }
    }
  }

  // handles mouse clicking and changes turn
  public void onMouseClicked(Posn position) {

    // determines x and y coordinates of cell being clicked on 
    int x = position.x / new GameConstants().cellSize;
    int y = position.y / new GameConstants().cellSize;

    // gets cell that was clicked
    Cell clickedCell = this.board.get(x).get(y);

    // changes color of clickedCell accordingly if needed 
    // (does nothing if clickedCell is not mtColor or is on edge of screen)
    if (clickedCell.color == new GameConstants().mtColor 
        && (x != 0 && x != this.boardSize - 1)
        && (y != 0 && y != this.boardSize - 1)) {

      // if it is player1's turn, changes cell to player1Color
      if (this.turn) {
        clickedCell.color = new GameConstants().player1Color;

      }

      // if it is player2's turn, changes cell to player2Color
      else {
        clickedCell.color = new GameConstants().player2Color;
      }

      // changes player turn after every valid click
      this.turn = !this.turn;

    }

  }



  // checks to see if a path exists from left to right
  boolean checkHorizontalPath() {

    // traverses cells along top horizontally 
    for (int x = 0; x < this.boardSize; x++) {
      Cell c = this.board.get(0).get(x);

      // call helper method that checks path from left to right on this cell
      if (c.checkHorizontalHelp(c, new GameConstants().player1Color, 
          new Stack<Cell>())) {
        return true;
      }
    }

    return false;
  }


  // checks to see if a path exists from top to bottom
  boolean checkVerticalPath() {

    // traverses cells along left side horizontally 
    for (int y = 0; y < this.boardSize; y++) {
      Cell c = this.board.get(y).get(0);

      // call helper method that checks path from top to bottom on this cell
      if (c.checkVerticalHelp(c, new GameConstants().player2Color, 
          new Queue<Cell>())) {
        return true;
      }
    }

    return false;
  }

  // checks to see if any path exists vertically or horizontally
  boolean checkPath() {
    return this.checkHorizontalPath() || this.checkVerticalPath();
  }

  // checks to see if any white spaces are still there
  boolean anyWhiteLeft() {

    //checks through each cell of board to see if any are white (exluding edges)
    for (int y = 1 ; y < (this.boardSize - 1); y++) {
      for (int x = 1; x < (this.boardSize - 1); x++) {

        // gets cell at every x and y coordinate as for loop loops
        Cell c = this.board.get(y).get(x);

        // returns true if any white cells remain
        if (c.color == (new GameConstants().mtColor)) {
          return true;
        }
      }
    }

    return false;
  }

  // ends game when a path is made or no white cells are left over
  boolean gameOver() {
    return checkPath() || !anyWhiteLeft();
  }

  // handles a pressed key
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.board = new ArrayList<>(); 
      this.initializeGame();
    }
  }


  // makes the scene of the game
  public WorldScene makeScene() {

    // assigns scene dimensions
    WorldScene scene = new WorldScene(this.board.get(1).size()
        * new GameConstants().cellSize, this.board.get(1).size() 
        * new GameConstants().cellSize);

    for (int i = 0; i < this.board.get(1).size(); i++) {
      for (int j = 0; j < this.board.get(1).size(); j++) {
        scene.placeImageXY(this.board.get(i).get(j).draw(), 
            (i * new GameConstants().cellSize) + 50, 
            (j * new GameConstants().cellSize) + 50);

      }

      // returns end scene if game ends
      if (this.gameOver()) {
        scene = this.makeEndScene();
      }

    }

    return scene;
  }

  // makes ending (winning/tie) scene of the game
  public WorldScene makeEndScene() {
    WorldScene scene = new WorldScene((this.boardSize * new GameConstants().cellSize), 
        this.boardSize * new GameConstants().cellSize);

    // if player 1 wins
    if (this.checkHorizontalPath()) {
      scene.placeImageXY(new TextImage("PLAYER 1 WINS!", 20, FontStyle.BOLD, 
          new GameConstants().player1Color), 
          ((this.boardSize * new GameConstants().cellSize) / 5), 
          ((this.boardSize * new GameConstants().cellSize) / 5));
    }

    // if player 2 wins
    else if (this.checkVerticalPath()) {
      scene.placeImageXY(new TextImage("PLAYER 2 WINS!", 20, FontStyle.BOLD, 
          new GameConstants().player2Color), 
          (this.boardSize * new GameConstants().cellSize) / 5, 
          (this.boardSize * new GameConstants().cellSize) / 5);
    }

    // if it's a tie
    else {
      scene.placeImageXY(new TextImage("IT'S A TIE!", 20, FontStyle.BOLD, 
          new GameConstants().mtColor), 
          ((this.boardSize * new GameConstants().cellSize) / 5), 
          ((this.boardSize * new GameConstants().cellSize) / 5));
    }

    return scene;

  }

}

//Represents a mutable collection of items
interface ICollection<T> {

  // Is this collection empty?
  boolean isEmpty();

  // EFFECT: adds the item to the collection
  void add(T t);

  // Returns the first item of the collection
  // EFFECT: removes that first item  
  T remove();
}

//represents a last-in first-out structure
class Stack<T> implements ICollection<T> {
  Deque<T> contents;

  // constructor
  Stack() {
    this.contents = new LinkedList<T>();
  }

  // Is this stack empty?
  @Override
  public boolean isEmpty() {
    return contents.isEmpty();
  }

  // EFFECT: adds the item to ICollection
  @Override
  public void add(T t) {
    contents.addFirst(t);
  }

  // EFFECT: removes that first item from ICollection
  @Override
  public T remove() {
    return contents.removeFirst();
  }

}

//represents a last-in first-out structure
class Queue<T> implements ICollection<T> {
  Deque<T> contents;

  // constructor for Queue which initializes the deque
  Queue() {
    this.contents = new LinkedList<T>();
  }

  // determines if queue is empty
  @Override
  public boolean isEmpty() {
    return this.contents.isEmpty();
  }

  // adds item to queue
  @Override
  public void add(T t) {
    this.contents.addLast(t);
  }

  //removes item from queue
  @Override
  public T remove() {
    return this.contents.removeFirst();
  }
}



//examples/test class
class ExamplesBridgIt {

  // cell examples
  Cell cellCyan;
  Cell cellBlue; 
  Cell cellWhiteAbove;
  Cell cellWhiteBelow;
  Cell cellWhiteRight;
  Cell cellWhiteLeft;

  Cell referenceCell;
  Cell testAbove;
  Cell testBelow;
  Cell testRight;
  Cell testLeft;

  Cell BCell1;
  Cell BCell2;
  Cell BCell3;
  Cell BCell4;
  Cell BCell5;
  Cell BCell6;
  Cell BCell7;
  Cell BCell8;
  Cell BCell9;

  Cell Cell1H;
  Cell Cell2H;
  Cell Cell3H;
  Cell Cell4H;
  Cell Cell5H;

  Cell Cell1V;
  Cell Cell2V;
  Cell Cell3V;
  Cell Cell4V;
  Cell Cell5V;

  BridgIt horizontalBoard = new BridgIt(5);
  BridgIt verticalBoard = new BridgIt(5);
  BridgIt normalBoard = new BridgIt(5);
  BridgIt fullBoard = new BridgIt(5);




  // board example
  ArrayList<ArrayList<Cell>> gameboard;
  ArrayList<Cell> column1;
  ArrayList<Cell> column2;
  ArrayList<Cell> column3;

  WorldScene scenePlayer1;
  WorldScene scenePlayer2;

  // cell initialization
  void initConditions() {

    cellCyan = new Cell(Color.CYAN, cellWhiteAbove, cellWhiteBelow, 
        cellWhiteRight, cellWhiteLeft, new Posn(300, 300));

    cellBlue = new Cell(Color.BLUE, null, cellWhiteRight, null, cellWhiteAbove,
        new Posn(300, 300));

    cellWhiteAbove = new Cell(Color.WHITE, null, cellCyan, null, cellBlue,
        new Posn(300, 400));
    cellWhiteBelow = new Cell(Color.WHITE, cellCyan, null, null, null,
        new Posn(300, 200));
    cellWhiteRight = new Cell(Color.WHITE, cellBlue, null, null, cellCyan,
        new Posn(400, 300));
    cellWhiteLeft = new Cell(Color.WHITE, null, null, cellCyan, null,
        new Posn(200, 300)); 

    referenceCell = new Cell(Color.CYAN, null, null, null, null,
        new Posn(300, 300));
    testAbove = new Cell(Color.WHITE, null, null, null, null,
        new Posn(300, 400));
    testBelow = new Cell(Color.WHITE, null, null, null, null,
        new Posn(300, 200));
    testRight = new Cell(Color.WHITE, null, null, null, null,
        new Posn(400, 300));
    testLeft = new Cell(Color.WHITE, null, null, null, null,
        new Posn(200, 300));

    BCell1 = new Cell(Color.BLUE, null, null, null, null, new Posn(50, 50)); //0,0
    BCell2 = new Cell(Color.WHITE, null, null, null, null, new Posn(150, 50)); //1,0
    BCell3 = new Cell(Color.BLUE, null, null, null, null, new Posn(250, 50)); //2,0

    BCell4 = new Cell(Color.WHITE, null, null, null, null, new Posn(50, 150)); //0,1
    BCell5 = new Cell(Color.CYAN, null, null, null, null, new Posn(150, 150)); //1,1
    BCell6 = new Cell(Color.WHITE, null, null, null, null, new Posn(250, 150)); //2,1

    BCell7 = new Cell(Color.BLUE, null, null, null, null, new Posn(50, 250)); //0,2
    BCell8 = new Cell(Color.WHITE, null, null, null, null, new Posn(150, 250)); //1,2
    BCell9 = new Cell(Color.BLUE, null, null, null, null, new Posn(250, 250)); //2,2

    gameboard = new ArrayList<ArrayList<Cell>>();

    column1 = new ArrayList<Cell>();
    column2 = new ArrayList<Cell>();
    column3 = new ArrayList<Cell>();

    column1.add(BCell3);
    column1.add(BCell2);
    column1.add(BCell1);

    column2.add(BCell6);
    column2.add(BCell5);
    column2.add(BCell4);

    column3.add(BCell9);
    column3.add(BCell8);
    column3.add(BCell7);

    gameboard.add(column1);
    gameboard.add(column2);
    gameboard.add(column3);


    // game ending scenes
    scenePlayer1 = new WorldScene(normalBoard.boardSize 
        * new GameConstants().cellSize, normalBoard.boardSize 
        * new GameConstants().cellSize);

    scenePlayer2 = new WorldScene(normalBoard.boardSize 
        * new GameConstants().cellSize, normalBoard.boardSize 
        * new GameConstants().cellSize);



    // Creates horizontal path:
    // changes white cells to player1color to make horizontal path
    Cell Cell1H = horizontalBoard.board.get(1).get(1);
    Cell1H.color = new GameConstants().player1Color;

    // changes white cells to player1color to make horizontal path
    Cell Cell2H = horizontalBoard.board.get(2).get(2);
    Cell2H.color = new GameConstants().player1Color;

    // changes white cells to player1color to make horizontal path
    Cell Cell3H = horizontalBoard.board.get(3).get(3);
    Cell3H.color = new GameConstants().player1Color;

    // changes white cells to player2color 
    Cell Cell4H = horizontalBoard.board.get(0).get(2);
    Cell4H.color = new GameConstants().player2Color;

    // changes white cells to player2color 
    Cell Cell5H = horizontalBoard.board.get(1).get(3);
    Cell5H.color = new GameConstants().player2Color;


    // Creates vertical path:
    // changes white cells to player1color 
    Cell Cell1V = verticalBoard.board.get(1).get(1);
    Cell1V.color = new GameConstants().player1Color;

    // changes white cells to player1color 
    Cell Cell2V = verticalBoard.board.get(2).get(2);
    Cell2V.color = new GameConstants().player1Color;

    // changes white cells to player2color to make vertical path
    Cell Cell3V = verticalBoard.board.get(1).get(3);
    Cell3V.color = new GameConstants().player2Color;

    // changes white cells to player2color to make vertical path
    Cell Cell4V = verticalBoard.board.get(2).get(2);
    Cell4V.color = new GameConstants().player2Color;

    // changes white cells to player2color to make vertical path
    Cell Cell5V = verticalBoard.board.get(3).get(1);
    Cell5V.color = new GameConstants().player2Color;


    // Creates a full board with no white space
    Cell Cell1 = fullBoard.board.get(1).get(1);
    Cell1.color = new GameConstants().player1Color;

    Cell Cell2 = fullBoard.board.get(1).get(3);
    Cell2.color = new GameConstants().player2Color;

    Cell Cell3 = fullBoard.board.get(2).get(2);
    Cell3.color = new GameConstants().player1Color;

    Cell Cell4 = fullBoard.board.get(3).get(1);
    Cell4.color = new GameConstants().player2Color;

    Cell Cell5 = fullBoard.board.get(3).get(3);
    Cell5.color = new GameConstants().player1Color;



  }

  // tests draw
  boolean testDraw(Tester t) {
    initConditions();
    return 
        t.checkExpect(cellCyan.draw(), new RectangleImage(new GameConstants().cellSize, 
            new GameConstants().cellSize, OutlineMode.SOLID, Color.CYAN))
        && t.checkExpect(cellBlue.draw(), new RectangleImage(new GameConstants().cellSize, 
            new GameConstants().cellSize, OutlineMode.SOLID, Color.BLUE))
        && t.checkExpect(cellWhiteAbove.draw(), 
            new RectangleImage(new GameConstants().cellSize, 
                new GameConstants().cellSize, OutlineMode.SOLID, Color.WHITE));
  }

  // tests set location
  boolean testSetLocation(Tester t) {
    initConditions();
    referenceCell.setLocation(testAbove, testBelow, testRight, testLeft);
    return
        t.checkExpect(referenceCell.above.equals(testAbove), true) 
        && t.checkExpect(referenceCell.below.equals(testBelow), true) 
        && t.checkExpect(referenceCell.left.equals(testLeft), true) 
        && t.checkExpect(referenceCell.right.equals(testRight), true);
  }

  // tests update location
  void testUpdateLocation(Tester t) {
    initConditions();
    BridgIt board1 = new BridgIt(gameboard, true, 5);
    board1.updateLocation();

    t.checkExpect(BCell5.above.equals(BCell2), true);
    t.checkExpect(BCell5.below.equals(BCell8), true);
    t.checkExpect(BCell5.right.equals(BCell6), true);
    t.checkExpect(BCell5.left.equals(BCell4), true);
  }

  // tests onMouseClick
  void testOnMouseClicked(Tester t) {
    Posn mouse1 = new Posn(150, 150); // 5x5 board 
    Posn mouse2 = new Posn(250, 250); // 5x5 board`
    Posn mouse3 = new Posn(200, 200); // 5x5 board
    Posn mouse4 = new Posn(50, 50); // 5x5 board on the edges of the board 

    BridgIt boardTest = new BridgIt();

    t.checkExpect(boardTest.board.get(1).get(1).color, Color.white);
    // player1 color change test
    boardTest.onMouseClicked(mouse1);
    t.checkExpect(boardTest.board.get(1).get(1).color, Color.cyan);
    // player2 color change test
    boardTest.onMouseClicked(mouse2);
    t.checkExpect(boardTest.board.get(2).get(2).color, Color.blue);
    // 200x200 test (right on corner) would return white
    boardTest.onMouseClicked(mouse3);
    t.checkExpect(boardTest.board.get(3).get(3).color, Color.white);
    // white cell on edge. should do nothing so the color is white
    boardTest.onMouseClicked(mouse4);
    t.checkExpect(boardTest.board.get(0).get(0).color, Color.white);
  }

  // tests checkHorizontalHelp
  void testCheckHorizontalHelp(Tester t) {
    initConditions();

    t.checkExpect(horizontalBoard.board.get(2).get(2)
        .checkHorizontalHelp(horizontalBoard.board.get(2).get(2), 
            new GameConstants().player1Color, new Stack<Cell>()), true);
    t.checkExpect(horizontalBoard.board.get(3).get(3)
        .checkHorizontalHelp(horizontalBoard.board.get(3).get(3), 
            new GameConstants().player1Color, new Stack<Cell>()), true);
    t.checkExpect(horizontalBoard.board.get(0).get(0)
        .checkHorizontalHelp(horizontalBoard.board.get(0).get(0), 
            new GameConstants().player1Color, new Stack<Cell>()), false);
  }


  // tests checkVerticalHelp
  void testCheckVerticalHelp(Tester t) {
    initConditions();


    t.checkExpect(verticalBoard.board.get(3).get(3)
        .checkVerticalHelp(verticalBoard.board.get(3).get(3), 
            new GameConstants().player2Color, new Queue<Cell>()), false);
    t.checkExpect(verticalBoard.board.get(3).get(1)
        .checkVerticalHelp(verticalBoard.board.get(3).get(1), 
            new GameConstants().player2Color, new Queue<Cell>()), true);
    t.checkExpect(verticalBoard.board.get(2).get(2)
        .checkVerticalHelp(verticalBoard.board.get(2).get(2), 
            new GameConstants().player2Color, new Queue<Cell>()), true);
  }

  // tests checkHorizontalPath
  void testCheckHorizontalPath(Tester t) {
    initConditions();

    t.checkExpect(horizontalBoard.checkHorizontalPath(), true);
    t.checkExpect(normalBoard.checkHorizontalPath(), false);
  }

  // tests checkVerticalPath
  void testCheckVerticalPath(Tester t) {
    initConditions();

    t.checkExpect(verticalBoard.checkVerticalPath(), true);
    t.checkExpect(normalBoard.checkVerticalPath(), false);
  }

  // tests checkPath
  void testCheckPath(Tester t) {
    initConditions();

    t.checkExpect(horizontalBoard.checkPath(), true);
    t.checkExpect(verticalBoard.checkPath(), true);
    t.checkExpect(normalBoard.checkPath(), false);

  }

  // tests anyWhiteLeft
  void testAnyWhiteLeft(Tester t) {
    initConditions();


    t.checkExpect(fullBoard.anyWhiteLeft(), false);
    t.checkExpect(normalBoard.anyWhiteLeft(), true);

  }

  // tests gameOver
  void testGameOver(Tester t) {
    initConditions();


    t.checkExpect(verticalBoard.gameOver(), true);
    t.checkExpect(fullBoard.gameOver(), true);
    t.checkExpect(normalBoard.gameOver(), false);

  }

  //tests the onKeyEvent method
  void testOnKeyEvent(Tester t) {
    initConditions();

    verticalBoard.onKeyEvent("r");
    t.checkExpect(verticalBoard.checkPath(), false);

    fullBoard.onKeyEvent("r");
    t.checkExpect(fullBoard.anyWhiteLeft(), true);
  }


  // tests makeEndScene
  void testMakeEndScene(Tester t) {
    initConditions();

    scenePlayer1.placeImageXY(new TextImage("PLAYER 1 WINS!", 20, FontStyle.BOLD, 
        new GameConstants().player1Color), 
        (horizontalBoard.boardSize * new GameConstants().cellSize) / 5, 
        (horizontalBoard.boardSize * new GameConstants().cellSize) / 5);


    scenePlayer2.placeImageXY(new TextImage("PLAYER 2 WINS!", 20, FontStyle.BOLD, 
        new GameConstants().player2Color), 
        (verticalBoard.boardSize * new GameConstants().cellSize) / 5, 
        (verticalBoard.boardSize* new GameConstants().cellSize) / 5);


    t.checkExpect(horizontalBoard.makeEndScene(), scenePlayer1);
    t.checkExpect(verticalBoard.makeEndScene(), scenePlayer2);

  }

  // tests makescene
  void testMakeScene(Tester t) {
    initConditions();

    BridgIt board2 = new BridgIt();
    WorldScene scene2 = new WorldScene(500, 500);

    // scene 2
    scene2.placeImageXY(board2.board.get(0).get(0).draw(), 
        (0 * 100) + 50, (0 * 100) + 50);
    scene2.placeImageXY(board2.board.get(0).get(1).draw(), 
        (0 * 100) + 50, (1 * 100) + 50);
    scene2.placeImageXY(board2.board.get(0).get(2).draw(),
        (0 * 100) + 50, (2 * 100) + 50);
    scene2.placeImageXY(board2.board.get(0).get(3).draw(), 
        (0 * 100) + 50, (3 * 100) + 50);
    scene2.placeImageXY(board2.board.get(0).get(4).draw(),
        (0 * 100) + 50, (4 * 100) + 50);
    scene2.placeImageXY(board2.board.get(1).get(0).draw(), 
        (1 * 100) + 50, (0 * 100) + 50);
    scene2.placeImageXY(board2.board.get(1).get(1).draw(), 
        (1 * 100) + 50, (1 * 100) + 50);
    scene2.placeImageXY(board2.board.get(1).get(2).draw(), 
        (1 * 100) + 50, (2 * 100) + 50);
    scene2.placeImageXY(board2.board.get(1).get(3).draw(), 
        (1 * 100) + 50, (3 * 100) + 50);
    scene2.placeImageXY(board2.board.get(1).get(4).draw(),
        (1 * 100) + 50, (4 * 100) + 50);
    scene2.placeImageXY(board2.board.get(2).get(0).draw(), 
        (2 * 100) + 50, (0 * 100) + 50);
    scene2.placeImageXY(board2.board.get(2).get(1).draw(), 
        (2 * 100) + 50, (1 * 100) + 50);
    scene2.placeImageXY(board2.board.get(2).get(2).draw(), 
        (2 * 100) + 50, (2 * 100) + 50);
    scene2.placeImageXY(board2.board.get(2).get(3).draw(), 
        (2 * 100) + 50, (3 * 100) + 50);
    scene2.placeImageXY(board2.board.get(2).get(4).draw(),
        (2 * 100) + 50, (4 * 100) + 50);
    scene2.placeImageXY(board2.board.get(3).get(0).draw(), 
        (3 * 100) + 50, (0 * 100) + 50);
    scene2.placeImageXY(board2.board.get(3).get(1).draw(), 
        (3 * 100) + 50, (1 * 100) + 50);
    scene2.placeImageXY(board2.board.get(3).get(2).draw(), 
        (3 * 100) + 50, (2 * 100) + 50);
    scene2.placeImageXY(board2.board.get(3).get(3).draw(), 
        (3 * 100) + 50, (3 * 100) + 50);
    scene2.placeImageXY(board2.board.get(3).get(4).draw(),
        (3 * 100) + 50, (4 * 100) + 50);
    scene2.placeImageXY(board2.board.get(4).get(0).draw(), 
        (4 * 100) + 50, (0 * 100) + 50);
    scene2.placeImageXY(board2.board.get(4).get(1).draw(), 
        (4 * 100) + 50, (1 * 100) + 50);
    scene2.placeImageXY(board2.board.get(4).get(2).draw(), 
        (4 * 100) + 50, (2 * 100) + 50);
    scene2.placeImageXY(board2.board.get(4).get(3).draw(), 
        (4 * 100) + 50, (3 * 100) + 50);
    scene2.placeImageXY(board2.board.get(4).get(4).draw(),
        (4 * 100) + 50, (4 * 100) + 50);

    t.checkExpect(board2.makeScene(), scene2);
  }



  //tests big bang
  void testBigBang(Tester t) {

    BridgIt startGame = new BridgIt(5);
    startGame.bigBang(startGame.boardSize * new GameConstants().cellSize, 
        startGame.boardSize * new GameConstants().cellSize);
  }
}


