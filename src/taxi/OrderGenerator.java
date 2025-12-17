package taxi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class OrderGenerator extends Thread {
    private final BlockingQueue<Order> queue;
    private final int delayMs;
    private final int countOrders;

    private int orderId = 1;

    public OrderGenerator(BlockingQueue<Order> queue, int delayMs, int countOrders) {
        this.queue = queue;
        this.delayMs = delayMs;
        this.countOrders = countOrders;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < countOrders; i++) {
                Order order = generateOrder();
                queue.put(order);

                System.out.println(
                        "Создан заказ " + order.getId() +
                                " | (" + order.getFromX() + "," + order.getFromY() +
                                ") → (" + order.getToX() + "," + order.getToY() + ")"
                );

                Thread.sleep(delayMs);
            }

            queue.put(Order.poisonPill());
            System.out.println("Генератор создал все " + countOrders + " заказов и завершился");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Order generateOrder() {
        int fromX = randCoord();
        int fromY = randCoord();
        int toX = randCoord();
        int toY = randCoord();
        return new Order(orderId++, fromX, fromY, toX, toY);
    }

    private int randCoord() {
        return ThreadLocalRandom.current().nextInt(0, 50);
    }
}
