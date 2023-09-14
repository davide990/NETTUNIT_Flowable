package nettunit.handler.demo.it;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.MUSA.StateOfWorldUpdateOp;
import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.rabbitMQ.ConsumerService.MUSARabbitMQConsumerService;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class identifying_incident implements JavaDelegate, TriggerableActivityBehavior {
    String evolution_predicate = "identified_incident(volcano)";
    private static Logger logger = LoggerFactory.getLogger(identifying_incident.class);

    @Override
    public void trigger(DelegateExecution delegateExecution, String signalEvent, Object signalData) {
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        //String taskName = ((ExecutionEntityImpl) delegateExecution).getActivityName();
        nettunit.updateMUSAStateOfWorld(StateOfWorldUpdateOp.ADD, evolution_predicate, this.getClass().getName());
        logger.info("Capability executed correctly [" + delegateExecution.getId() + "]: " + this.getClass().getSimpleName());
    }

    @Override
    public void execute(DelegateExecution execution) {

        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        MUSARabbitMQConsumerService musaRabbitMQConsumerService = SpringContext.getBean(MUSARabbitMQConsumerService.class);
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);
        String taskName = ((ExecutionEntityImpl) execution).getActivityName();
        String taskID = ((ExecutionEntityImpl) execution).getActivityId();

        //nettunit.currentTask = Optional.of(this.getClass().getName());
        nettunit.FailedTaskName = Optional.of(taskName);
        nettunit.FailedTaskImplementation = Optional.of(this.getClass().getName());

        MUSAProducer.updateEventSeverity(evt, JixelDomainInformation.SEVERITY_LEVEL_STANDARD);
        musaRabbitMQConsumerService.save(evt, taskID);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName() + " Waiting for ack...");
    }
}
