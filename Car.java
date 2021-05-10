package rushhour;

public class Car {
    private char name;
    private int x;          // x-coordinate of top/left of car
    private int y;          // y-coordinate of top/left of car
    private int len;        // length of car itself
    private int direction;  // direction of car: up, left

    // possible orientations of car
    public static final int HORI = 0;
    public static final int VERT = 1;

    /**
     * default constructor
     */
    public Car() {
        name = ' ';
        x = 0;
        y = 0;
        len = 0;
        direction = HORI;
    }

    /**
     * deep copy constructor
     * @param otherCar
     */
    public Car(Car otherCar) {
        name = otherCar.getName();
        x = otherCar.getX();
        y = otherCar.getY();
        len = otherCar.getLength();
        direction = otherCar.getDir();
    }

    /**
     * constructor w/ custom data fields as params
     * @param letter
     * @param topX
     * @param topY
     * @param len
     * @param dir
     */
    public Car(char letter, int topX, int topY, int len, int dir) {
        name = letter;
        x = topX;
        y = topY;
        this.len = len;
        direction = dir;
    }

    // getters
    public char getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getLength() { return len; }
    public int getDir() { return direction; }

    // setters
    public void setName(char newName) { name = newName; }
    public void setX(int n) { x = n; }
    public void setY(int n) { y = n; }
    public void setSize(int n) { len = n; }
    public void setDir(int n) { direction = n; }
}
