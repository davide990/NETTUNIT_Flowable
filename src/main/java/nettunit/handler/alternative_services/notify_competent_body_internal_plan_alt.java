package nettunit.handler.alternative_services;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.SpringContext;
import nettunit.handler.notify_competent_body_internal_plan;
import nettunit.rabbitMQ.ConsumerService.JixelRabbitMQConsumerService;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.mutable.ArrayBuffer;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class notify_competent_body_internal_plan_alt implements JavaDelegate {

    private static Logger logger = LoggerFactory.getLogger(notify_competent_body_internal_plan.class);

    @Override
    public void execute(DelegateExecution execution) {

        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        logger.info("Executing capability: " + this.getClass().getSimpleName());


        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);
        String taskID = execution.getId();
        //jixelRabbitMQConsumerService.save(evt, taskID);
        //jixelRabbitMQConsumerService.save(evt, taskID);
        //MUSAProducer.addRecipient(evt, JixelDomainInformation.ASP);
        //MUSAProducer.addRecipient(evt, JixelDomainInformation.ARPA);
        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.ASP);
        recipients.addOne(JixelDomainInformation.ARPA);
        MUSAProducer.addRecipient(evt, recipients.toList());
        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}