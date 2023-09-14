package nettunit.TimeConstraint;

import RabbitMQ.JixelEvent;
import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.handler.demo.it.identifying_incident;
import nettunit.rabbitMQ.ConsumerService.MUSARabbitMQConsumerService;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class TSHandler {


    public boolean handle(String taskName, int thSeconds, JixelEvent evt, Class c) {

        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        MUSARabbitMQConsumerService musaRabbitMQConsumerService = SpringContext.getBean(MUSARabbitMQConsumerService.class);
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        Logger logger = LoggerFactory.getLogger(c);

        int i = 0;
        try {
            int num_pending_msg = -1;
            while (i < thSeconds) {
                num_pending_msg = musaRabbitMQConsumerService.getNumberOfPendingMessages(evt.id());
                if (num_pending_msg == 0) {
                    return true;
                }
                TimeUnit.SECONDS.sleep(1);
                logger.info("Waiting for #" + num_pending_msg + " acknowledge from Jixel.");
                i += 1;
            }

            if (num_pending_msg > 0) {
                nettunit.FailedTaskName = Optional.of(taskName);
                nettunit.FailedTaskImplementation = Optional.of(c.getName());
                throw new BpmnError("REQUIRE_ORCHESTRATION", c.getName());
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
