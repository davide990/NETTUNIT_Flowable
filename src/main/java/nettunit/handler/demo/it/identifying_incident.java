package nettunit.handler.demo.it;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.MUSA.StateOfWorldUpdateOp;
import nettunit.handler.base.BaseHandler;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.mutable.ArrayBuffer;

import java.util.Optional;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class identifying_incident extends BaseHandler implements TriggerableActivityBehavior {

    private static Logger logger = LoggerFactory.getLogger(identifying_incident.class);

    String evolution_predicate = "identified_incident(volcano)";

    @Override
    public void trigger(DelegateExecution delegateExecution, String signalEvent, Object signalData) {
        this.getNETTUNITService().updateMUSAStateOfWorld(StateOfWorldUpdateOp.ADD, evolution_predicate, this.getClass().getName());
        logger.info("Capability executed correctly [" + delegateExecution.getId() + "]: " + this.getClass().getSimpleName());
    }

    @Override
    public void execute(DelegateExecution execution) {

        super.execute(execution);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());

        String taskName = ((ExecutionEntityImpl) execution).getActivityName();
        String taskID = ((ExecutionEntityImpl) execution).getActivityId();

        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);

        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.DIRECTOR_ETNEA_OBSERVATORY);
        this.getMUSAService().addRecipient(evt, recipients.toList());
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getMUSAService().updateCommType(evt, JixelDomainInformation.COMM_TYPE_PREOPERATIVA);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateEventTypology(evt, JixelDomainInformation.EVENT_TYPE_VULCANO);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateUrgencyLevel(evt, JixelDomainInformation.URGENCY_LEVEL_FUTURA);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateEventSeverity(evt, JixelDomainInformation.SEVERITY_LEVEL_MINORE);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateEventDescription(evt, "test");
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getNETTUNITService().currentTask = Optional.of(this.getClass().getName());
        this.getNETTUNITService().FailedTaskName = Optional.of(taskName);
        this.getNETTUNITService().FailedTaskImplementation = Optional.of(this.getClass().getName());

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName() + " Waiting for ack...");

    }

}
