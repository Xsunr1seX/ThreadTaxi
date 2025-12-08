import java.util.List;

import static java.lang.Math.round;

public class Taxi extends Thread {
    private final int id;
    private volatile boolean free = true;
    private Order currentOrder;
    private int currentX, currentY;
    private final List<Taxi> taxis;


    public Taxi(int id, List<Taxi> taxis) {
        this.id = id;
        this.taxis = taxis;
    }
    public int getCurrentX(){return currentX;}
    public int getCurrentY(){return currentY;}


    public boolean isFree() {
        return free;
    }

    public synchronized void assignOrder(Order order) {
        while (!free) {
            try {
                wait();
            } catch (InterruptedException e) {
                return;
            }
        }

        this.currentOrder = order;
        this.free = false;

        System.out.println("Такси " + id + " получил заказ " + order.getId());
        notifyAll();
    }


    @Override
    public void run() {
        while (true) {
            try {
                synchronized (this) {
                    while (currentOrder == null) {
                        wait();
                    }
                }

                int distanceBefore = distanceBetween(
                        this.currentX, this.currentY,
                        currentOrder.getFromX(), currentOrder.getFromY()
                );
                System.out.println("Такси " + id +
                        " едет к клиенту (" + round(distanceBefore * 0.1) + " км). " +
                        "От (" + currentX + "," + currentY + ") → (" +
                        currentOrder.getFromX() + "," + currentOrder.getFromY() + ")");

                Thread.sleep(distanceBefore * 100);

                int distanceAfter = distanceBetween(
                        currentOrder.getFromX(), currentOrder.getFromY(),
                        currentOrder.getToX(), currentOrder.getToY()
                );
                System.out.println("Такси " + id +
                        " перевозит клиента (" + round(distanceAfter * 0.1) + " км). " +
                        "От (" + currentOrder.getFromX() + "," + currentOrder.getFromY() +
                        ") → (" + currentOrder.getToX() + "," + currentOrder.getToY() + ")");

                Thread.sleep(distanceAfter * 100);

                System.out.println("Такси " + id + " завершило заказ " + currentOrder.getId() +
                        ". Итоговая точка: (" + currentOrder.getToX() + "," + currentOrder.getToY() + ")");


                synchronized (taxis) {
                    this.currentX = currentOrder.getToX();
                    this.currentY = currentOrder.getToY();

                    currentOrder = null;
                    free = true;
                    taxis.notifyAll();
                }

            } catch (InterruptedException e) {
                return;
            }
        }
    }

    public int distanceBetween(int fromX, int fromY, int toX, int toY){
        double distance = round(Math.sqrt(Math.pow(toX - fromX,2) + Math.pow(toY - fromY,2)));
        return (int) distance;
    }
}
