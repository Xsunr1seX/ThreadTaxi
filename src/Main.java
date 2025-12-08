import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws Exception {

        BlockingQueue<Order> queue = new LinkedBlockingQueue<>();
        List<Taxi> taxis = new ArrayList<>();

        Taxi t1 = new Taxi(1, taxis);
        Taxi t2 = new Taxi(2, taxis);
        Taxi t3 = new Taxi(3, taxis);

        taxis.add(t1);
        taxis.add(t2);
        taxis.add(t3);

        t1.start();
        t2.start();
        t3.start();

        Dispatcher dispatcher = new Dispatcher(queue, taxis);
        dispatcher.start();


        for (int i = 1; i <= 10; i++) {
            Order o = new Order(
                    i,
                    (int)(Math.random() * 50),
                    (int)(Math.random() * 50),
                    (int)(Math.random() * 50),
                    (int)(Math.random() * 50)
            );

            queue.put(o);
            Thread.sleep(200);
        }
    }
}
