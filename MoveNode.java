package rushhour;

public class MoveNode {
    private int dir;                // forward/backward
    private String moveName;        // e.g. XD1 (X car go down 1)
    private MoveNode parentMove;    // move that was last made before this one


    /**
     * constructor
     */
    public MoveNode(int newDir, Car c, MoveNode newParent) {
        dir = newDir;
        switch(newDir) {
            case Board.BACKWARD:
                if (c.getDir() == Car.HORI)
                    moveName = c.getName() + "L1";
                else if (c.getDir() == Car.VERT)
                    moveName = c.getName() + "U1";
                break;
            case Board.FORWARD:
                if (c.getDir() == Car.HORI)
                    moveName = c.getName() + "R1";
                else if (c.getDir() == Car.VERT)
                    moveName = c.getName() + "D1";
                break;
        }
        parentMove = newParent;
    }

    // getters
    public int getDir() { return dir; }
    public String getMoveName() { return moveName; }
    public MoveNode getParentMove() { return parentMove; }

    // setters
    public void setDir(int newDir) { dir = newDir; }
    public void setMoveName(String newName) { moveName = newName; }
    public void setParentMove(MoveNode newParent) { parentMove = newParent; }
}
