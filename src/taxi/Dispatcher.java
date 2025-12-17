package taxi;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class Dispatcher extends Thread {
    private final BlockingQueue<Order> queue;
    private final List<Taxi> taxis;

    private final int totalOrders;
    private final CountDownLatch doneLatch;
    private final AtomicBoolean shutdown;

    public Dispatcher(BlockingQueue<Order> queue, List<Taxi> taxis,
                      int totalOrders, CountDownLatch doneLatch, AtomicBoolean shutdown) {
        this.queue = queue;
        this.taxis = taxis;
        this.totalOrders = totalOrders;
        this.doneLatch = doneLatch;
        this.shutdown = shutdown;
        setName("taxi.Dispatcher");
    }

    @Override
    public void run() {
        boolean gotPoison = false;

        try {
            while (true) {
                Order order = queue.take();


                if (order.isPoisonPill()) {
                    gotPoison = true;
                    System.out.println("Диспетчер: получил poison pill.");
                    break;
                }

                Taxi chosenTaxi;
                synchronized (taxis) {
                    while ((chosenTaxi = findFreeTaxi(order)) == null) {
                        taxis.wait();
                    }
                }
                chosenTaxi.assignOrder(order);
            }

            if (gotPoison) {
                System.out.println("Диспетчер: жду завершения всех заказов...");
                doneLatch.await();
                System.out.println("Диспетчер: все " + totalOrders + " заказов выполнены.");
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            shutdown.set(true);
            for (Taxi t : taxis) t.requestShutdown();
            System.out.println("Диспетчер: shutdown отправлен всем такси.");
        }
    }

    private Taxi findFreeTaxi(Order order) {
        Taxi best = null;
        int bestDist = Integer.MAX_VALUE;

        for (Taxi t : taxis) {
            if (!t.isFree()) continue;

            int dist = t.distanceBetween(
                    t.getCurrentX(), t.getCurrentY(),
                    order.getFromX(), order.getFromY()
            );

            if (dist < bestDist) {
                bestDist = dist;
                best = t;
            }
        }
        return best;
    }
}
