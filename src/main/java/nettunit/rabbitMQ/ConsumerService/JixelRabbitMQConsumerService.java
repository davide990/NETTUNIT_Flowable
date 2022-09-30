package nettunit.rabbitMQ.ConsumerService;

import RabbitMQ.Consumer.JixelRabbitMQConsumer;
import RabbitMQ.JixelEvent;
import RabbitMQ.JixelEventUpdate;
import RabbitMQ.Listener.JixelConsumerListener;
import RabbitMQ.Recipient;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Some;

import javax.annotation.PostConstruct;

@Service
/**
 * This service handles the Jixel consumer service. Specifically, it serves as a listener for knowing when a message
 * sent by musa (through a specific capability) is consumed by the recipient.
 */
public class JixelRabbitMQConsumerService extends Consumer {

    private static JixelRabbitMQConsumer consumer = new JixelRabbitMQConsumer();

    @Autowired
    public JixelRabbitMQConsumerService() {
        super();
        logger = LoggerFactory.getLogger(JixelRabbitMQConsumerService.class);
        consumerTask = new Thread(() -> {
            consumer.init();
            consumer.startConsumerAndAwait(MAXIMUM_CONSUMER_MESSAGES_COUNT, new Some<>(new JixelConsumerListener() {
                @Override
                public void onCreateEvent(JixelEvent event) {
                    completeTask(event);
                }

                @Override
                public void onAddRecipient(Recipient r) {
                    completeTask(r);
                }

                @Override
                public void onEventUpdate(JixelEventUpdate update) {
                    completeTask(update);
                }

            }));
        });
    }

    @PostConstruct
    public void init() {
        consumerTask.start();
    }


}
