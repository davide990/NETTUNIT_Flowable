package nettunit.rabbitMQ.ConsumerService;

import RabbitMQ.*;
import RabbitMQ.Consumer.MUSARabbitMQConsumer;
import RabbitMQ.Listener.MUSAConsumerListener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Some;

import javax.annotation.PostConstruct;

@Service
public class MUSARabbitMQConsumerService extends Consumer {

    private static MUSARabbitMQConsumer consumer = new MUSARabbitMQConsumer();

    @Autowired
    public MUSARabbitMQConsumerService() {
        super();
        /**
         * Create a new consumer for the events that are consumed by MUSA (produced by Jixel).
         * The consumer is associated to a listener, whose methods are invoked when a message produced by jixel is
         * consumed by musa.
         */
        consumerTask = new Thread(() -> {
            consumer.init();
            consumer.startConsumerAndAwait(MAXIMUM_CONSUMER_MESSAGES_COUNT, new Some<>(new MUSAConsumerListener() {
                @Override
                public void onNotifyEvent(JixelEvent event) {
                    //TODO here, should I create a new process instance?
                    //nettunitService.applyInterventionRequest(event);
                    applyInterventionRequest(event);
                }

                @Override
                public void onNotifyEventSummary(JixelEventSummary event) {
                    //TODO here, should I create a new process instance?
                    //nettunitService.applyInterventionRequest(event);
                }

                @Override
                public void onEventUpdate(JixelEventUpdate update) {
                    completeTaskByEvent(update.event());
                }

                @Override
                public void onReceiveJixelReport(JixelEventReport report) {
                    completeTaskByEvent(report.event());
                }

                @Override
                public void onAddRecipient(Recipient r) {
                    completeTaskByEvent(r.event());
                }

                @Override
                public void onJixelAckAddRecipient(JixelAckAddRecipient msg) {
                    completeTaskByEvent(msg.original_message().data().incident_id());
                }

                @Override
                public void onJixelAckUrgencyLevel(JixelAckUrgencyLevel msg) {
                    completeTaskByEvent(msg.original_message().data().incident_id());
                }

                @Override
                public void onJixelAckEventSeverity(JixelAckEventSeverity msg) {
                    completeTaskByEvent(msg.original_message().data().incident_id());
                }

                @Override
                public void onJixelAckEventTypology(JixelAckEventTypology msg) {
                    completeTaskByEvent(msg.original_message().data().incident_id());
                }

                @Override
                public void onJixelAckEventDescription(JixelAckEventDescription msg) {
                    System.out.println("received ack");
                    completeTaskByEvent(msg.original_message().data().incident_id());
                }

                @Override
                public void onJixelAckUpdateCommType(JixelAckUpdateCommType msg) {
                    completeTaskByEvent(msg.original_message().data().incident_id());
                }

                @Override
                public void onJixelAckUpdateCommTypeError(JixelAckUpdateCommTypeError msg) {
                    completeTaskByEvent(msg.original_message().data().incident_id());
                }
            }));
        });
    }

    @PostConstruct
    public void init() {
        consumerTask.start();
    }

}
