package nettunit.rabbitMQ.ConsumerService;

import RabbitMQ.Consumer.JixelRabbitMQConsumer;
import RabbitMQ.JixelEvent;
import RabbitMQ.JixelEventSummary;
import RabbitMQ.JixelEventUpdate;
import RabbitMQ.Listener.JixelConsumerListener;
import RabbitMQ.Recipient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Some;

import javax.annotation.PostConstruct;

//@Service
/**
 * This service handles the Jixel consumer service. Specifically, it serves as a listener for knowing when a message
 * sent by musa (through a specific capability) is consumed by the recipient.
 */
public class JixelRabbitMQConsumerService extends Consumer {

    private static JixelRabbitMQConsumer consumer = new JixelRabbitMQConsumer();

    @Autowired
    public JixelRabbitMQConsumerService() {
        super();

        consumerTask = new Thread(() -> {
            consumer.init();
            consumer.startConsumerAndAwait(MAXIMUM_CONSUMER_MESSAGES_COUNT, new Some<>(new JixelConsumerListener() {
                @Override
                public void onCreateEvent(JixelEvent event) {
                    completeTaskByEvent(event);
                }

                @Override
                public void onCreateEventSummary(JixelEventSummary event) {

                    //TODO event from summary
                }

                @Override
                public void onAddRecipient(Recipient r) {
                    completeTaskByEvent(r.event());
                }

                @Override
                public void onEventUpdate(JixelEventUpdate update) {
                    completeTaskByEvent(update.event());
                }
            }));
        });
    }

    @PostConstruct
    public void init() {
        consumerTask.start();
    }


}
