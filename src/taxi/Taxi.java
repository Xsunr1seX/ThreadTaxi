package taxi;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.round;

public class Taxi extends Thread {
    private final int id;

    private volatile boolean free = true;
    private Order currentOrder;

    private int currentX, currentY;

    private final List<Taxi> taxis;
    private final CountDownLatch doneLatch;
    private final AtomicBoolean shutdown;

    public Taxi(int id, List<Taxi> taxis, CountDownLatch doneLatch, AtomicBoolean shutdown) {
        this.id = id;
        this.taxis = taxis;
        this.doneLatch = doneLatch;
        this.shutdown = shutdown;
        setName("taxi.Taxi-" + id);
    }

    public int getCurrentX() { return currentX; }
    public int getCurrentY() { return currentY; }
    public boolean isFree() { return free; }

    public synchronized void assignOrder(Order order) {
        while (!free) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        currentOrder = order;
        free = false;
        System.out.println("Такси " + id + " получил заказ " + order.getId());
        notifyAll();
    }

    public void requestShutdown() {
        shutdown.set(true);
        synchronized (this) {
            notifyAll();
        }
    }

    @Override
    public void run() {
        while (true) {
            Order orderToDo;

            try {
                synchronized (this) {
                    while (currentOrder == null && !shutdown.get()) {
                        wait();
                    }
                    if (currentOrder == null && shutdown.get()) {
                        return;
                    }
                    orderToDo = currentOrder;
                }

                int distanceBefore = distanceBetween(
                        currentX, currentY,
                        orderToDo.getFromX(), orderToDo.getFromY()
                );

                System.out.println("Такси " + id +
                        " едет к клиенту (" + round(distanceBefore * 0.1) + " км). " +
                        "От (" + currentX + "," + currentY + ") → (" +
                        orderToDo.getFromX() + "," + orderToDo.getFromY() + ")");

                Thread.sleep(distanceBefore * 100L);

                int distanceAfter = distanceBetween(
                        orderToDo.getFromX(), orderToDo.getFromY(),
                        orderToDo.getToX(), orderToDo.getToY()
                );

                System.out.println("Такси " + id +
                        " перевозит клиента (" + round(distanceAfter * 0.1) + " км). " +
                        "От (" + orderToDo.getFromX() + "," + orderToDo.getFromY() +
                        ") → (" + orderToDo.getToX() + "," + orderToDo.getToY() + ")");

                Thread.sleep(distanceAfter * 100L);

                System.out.println("Такси " + id + " завершил заказ " + orderToDo.getId());

                currentX = orderToDo.getToX();
                currentY = orderToDo.getToY();

                doneLatch.countDown();

                synchronized (this) {
                    currentOrder = null;
                    free = true;
                    notifyAll();
                }

                synchronized (taxis) {
                    taxis.notifyAll();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    public int distanceBetween(int fromX, int fromY, int toX, int toY) {
        double distance = round(Math.sqrt(Math.pow(toX - fromX, 2) + Math.pow(toY - fromY, 2)));
        return (int) distance;
    }
}
