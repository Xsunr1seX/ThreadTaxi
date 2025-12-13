import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.print("Введите количество такси: ");
        int taxiCount = scanner.nextInt();

        System.out.print("Введите количество заказов: ");
        int orderCount = scanner.nextInt();

        if (taxiCount <= 0 || orderCount <= 0) {
            System.out.println("Количество должно быть больше 0");
            return;
        }


        OrderGenerator generator = getOrderGenerator(taxiCount, orderCount);
        generator.start();

        System.out.println("\n Система запущена");
    }

    private static OrderGenerator getOrderGenerator(int taxiCount, int orderCount) {
        BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
        List<Taxi> taxis = new ArrayList<>();

        for (int i = 1; i <= taxiCount; i++) {
            Taxi taxi = new Taxi(i, taxis);
            taxis.add(taxi);
            taxi.start();
        }

        Dispatcher dispatcher = new Dispatcher(queue, taxis);
        dispatcher.start();

        return new OrderGenerator(queue, 300, orderCount);
    }
}
