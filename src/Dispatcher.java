import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Dispatcher extends Thread {

    private final BlockingQueue<Order> queue;
    private final List<Taxi> taxis;

    public Dispatcher(BlockingQueue<Order> queue, List<Taxi> taxis) {
        this.queue = queue;
        this.taxis = taxis;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Order order = queue.take();

                Taxi chosenTaxi;

                synchronized (taxis) {
                    while ((chosenTaxi = findFreeTaxi(order)) == null) {
                        taxis.wait();
                    }
                }


                chosenTaxi.assignOrder(order);

            } catch (InterruptedException e) {
                return;
            }
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

