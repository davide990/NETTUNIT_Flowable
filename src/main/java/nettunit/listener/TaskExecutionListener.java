package nettunit.listener;

import RabbitMQ.Consumer.MUSARabbitMQConsumer;
import RabbitMQ.JixelEvent;
import RabbitMQ.JixelEventReport;
import RabbitMQ.JixelEventUpdate;
import RabbitMQ.Listener.JixelConsumerListener;
import RabbitMQ.Listener.MUSAConsumerListener;
import RabbitMQ.Producer.JixelRabbitMQProducer;
import RabbitMQ.Producer.MUSARabbitMQProducer;
import RabbitMQ.Recipient;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import scala.Option;
import scala.Some;

public class TaskExecutionListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        String myName = execution.getCurrentFlowElement().getName();
        String myID = execution.getCurrentFlowElement().getId();

        System.out.println("process id: " + execution.getId());
/*
        MUSAConsumerListener listener = new MUSAConsumerListener() {
            @Override
            public void onReceiveJixelEvent(JixelEvent event) {

            }

            @Override
            public void onReceiveJixelEventUpdate(JixelEventUpdate update) {

            }

            @Override
            public void onReceiveJixelReport(JixelEventReport report) {

            }
        };

        MUSARabbitMQProducer MUSA = new MUSARabbitMQProducer();
        MUSARabbitMQConsumer consumer = new MUSARabbitMQConsumer();
        consumer.startConsumerAndAwait(100, new Some<>(listener));
*/




        System.out.println("[NOTIFICATION] started/ended task: \"" + myName + "\" with ID: " + myID);
    }
}


