package taxi;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите количество такси: ");
        int taxiCount = scanner.nextInt();

        System.out.print("Введите количество заказов: ");
        int orderCount = scanner.nextInt();

        if (taxiCount <= 0 || orderCount <= 0) {
            System.out.println("Количество должно быть больше 0");
            return;
        }

        BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
        AtomicBoolean shutdown = new AtomicBoolean(false);
        CountDownLatch doneLatch = new CountDownLatch(orderCount);

        List<Taxi> taxis = new ArrayList<>();

        for (int i = 1; i <= taxiCount; i++) {
            Taxi taxi = new Taxi(i, taxis, doneLatch, shutdown);
            taxis.add(taxi);
            taxi.start();
        }

        Dispatcher dispatcher = new Dispatcher(queue, taxis, orderCount, doneLatch, shutdown);
        dispatcher.start();

        OrderGenerator generator = new OrderGenerator(queue, 300, orderCount);
        generator.start();

        System.out.println("\nСистема запущена\n");

        generator.join();
        dispatcher.join();
        for (Taxi t : taxis) t.join();

        System.out.println("\nВсе потоки завершены. Завершение программы.");
    }
}
