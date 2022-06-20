package nettunit.rabbitMQ.ConsumerService;

import RabbitMQ.Consumer.JixelRabbitMQConsumer;
import RabbitMQ.JixelEvent;
import RabbitMQ.JixelEventUpdate;
import RabbitMQ.Listener.JixelConsumerListener;
import RabbitMQ.Recipient;
import nettunit.rabbitMQ.PendingMessageComponentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Some;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
/**
 * This service handles the Jixel consumer service. Specifically, it serves as a listener for knowing when a message
 * sent by musa (a capability) is consumed by the recipient.
 */
public class JixelRabbitMQConsumerService {

    private static int MAXIMUM_CONSUMER_MESSAGES_COUNT = 100000;

    public Optional<PendingMessageComponentListener> listener;

    private static JixelRabbitMQConsumer consumer = new JixelRabbitMQConsumer();

    /**
     * This map stores all the jixel events/updates/reports sent by MUSA from a specific task
     * until the message has been acknowledged from destination actor. Once acknowledged, the activity
     * of the process that provided the communication is completed
     */
    private Map<Object, String> pendingMessages;


    private Thread consumerTask;

    private static Logger logger = LoggerFactory.getLogger(JixelRabbitMQConsumerService.class);

    @Autowired
    public JixelRabbitMQConsumerService() {
        pendingMessages = new HashMap<>();
        consumerTask = new Thread(new Runnable() {
            @Override
            public void run() {
                consumer.init();
                consumer.startConsumerAndAwait(MAXIMUM_CONSUMER_MESSAGES_COUNT, new Some<>(new JixelConsumerListener() {
                    @Override
                    public void onReceiveJixelEvent(JixelEvent event) {
                        completeTask(event);
                    }

                    @Override
                    public void onAddRecipient(Recipient r) {
                        completeTask(r);
                    }

                    @Override
                    public void onReceiveJixelEventUpdate(JixelEventUpdate update) {
                        completeTask(update);
                    }
                }));
            }


        });
    }

    @PostConstruct
    public void init() {
        consumerTask.start();
    }

    private void completeTask(Object obj) {
        String pendingTaskID = getTaskID(obj);
        if (!pendingTaskID.isEmpty()) {
            remove(obj);
            listener.ifPresent(l -> l.completeTask(pendingTaskID));
            logger.info("Completed Task with ID: " + pendingTaskID);
        }
    }

    public void setListener(PendingMessageComponentListener listener) {
        this.listener = Optional.of(listener);
    }


    public String getTaskID(Object obj) {
        return pendingMessages.getOrDefault(obj, "");
    }

    public boolean remove(Object obj) {
        String pendingTaskID = pendingMessages.getOrDefault(obj, "");
        if (!pendingTaskID.isEmpty()) {
            pendingMessages.remove(obj);
            return true;
        }
        return false;
    }

    public void save(Object obj, String taskID) {
        pendingMessages.put(obj, taskID);
    }


}
