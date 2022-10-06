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

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class notify_competent_body_internal_plan_alt implements JavaDelegate {

    private static Logger logger = LoggerFactory.getLogger(notify_competent_body_internal_plan.class);

    @Override
    public void execute(DelegateExecution execution) {
        JixelRabbitMQConsumerService jixelRabbitMQConsumerService = SpringContext.getBean(JixelRabbitMQConsumerService.class);
        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        logger.info("Executing capability: " + this.getClass().getSimpleName());


        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);
        String taskID = execution.getId();
        jixelRabbitMQConsumerService.save(evt, taskID);
        jixelRabbitMQConsumerService.save(evt, taskID);
        MUSAProducer.addRecipient(evt, JixelDomainInformation.ASP);
        MUSAProducer.addRecipient(evt, JixelDomainInformation.ARPA);

        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}