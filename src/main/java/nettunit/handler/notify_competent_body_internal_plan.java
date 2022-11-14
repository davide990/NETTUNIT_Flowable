package nettunit.handler;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.rabbitMQ.ConsumerService.JixelRabbitMQConsumerService;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.mutable.ArrayBuffer;

import java.util.Optional;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class notify_competent_body_internal_plan implements JavaDelegate {

    private static Logger logger = LoggerFactory.getLogger(notify_competent_body_internal_plan.class);

    @Override
    public void execute(DelegateExecution execution) {

        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        if (nettunit.FailingTaskName.isPresent()) {
            if (nettunit.FailingTaskName.get().equals(this.getClass().getName())) {
                String taskName = ((ExecutionEntityImpl) execution).getActivityName();
                String taskID = execution.getId();
                nettunit.FailedTaskName = Optional.of(taskName);
                nettunit.FailedTaskImplementation = Optional.of(this.getClass().getName());
                throw new BpmnError("REQUIRE_ORCHESTRATION",this.getClass().getName());
            }
        }
        logger.info("Executing capability ["+execution.getId()+"]: " + this.getClass().getSimpleName());


        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);
        String taskID = execution.getId();


        //
        //        recipients.addOne(JixelDomainInformation.MAYOR);
        //        recipients.addOne(JixelDomainInformation.PREFECT);
        //        recipients.addOne(JixelDomainInformation.COMMANDER_FIRE_BRIGADE);
        //        MUSAProducer.addRecipient(evt, recipients.toList());
        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.ASP);
        recipients.addOne(JixelDomainInformation.ARPA);
        MUSAProducer.addRecipient(evt, recipients.toList());
        //MUSAProducer.addRecipient(evt, JixelDomainInformation.ARPA);

        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}