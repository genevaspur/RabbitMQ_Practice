import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class Worker {
    private final static String QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        boolean durable = true;
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);
        System.out.println(" [*] Waiting for messages....");

        int prefetchCount = 1;  // TODO consumer 메모리에 쌓아둘 job의 갯수(많을수록 느린작업일때 불리)
        channel.basicQos(prefetchCount);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            try {
                doWork(message);
            } catch (Exception e) {

            } finally {
                System.out.println(" [x] Done");

                // TODO autoAck 미사용 시 basicAck 명시 중요!!!!!(메모리 누수 가능성)
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        boolean autoAck = false;
        channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> {

        });
    }

    private static void doWork(String task) throws InterruptedException {
        for (char ch : task.toCharArray()) {
            if (ch == '.') {
                Thread.sleep(1000);
            }
        }
    }
}
