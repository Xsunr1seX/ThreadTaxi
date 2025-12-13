public class Order {
    private final int id;
    private final int fromX;
    private final int fromY;
    private final int toX;
    private final int toY;


    public Order(int id, int fromX, int fromY, int toX, int toY) {
        this.id = id;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }





    public int getId() { return id; }
    public int getFromX() { return fromX; }
    public int getFromY() { return fromY; }
    public int getToX() { return toX; }
    public int getToY() { return toY; }
}
