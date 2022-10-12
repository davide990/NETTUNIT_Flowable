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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class do_crossborder_communication implements JavaDelegate {

    private static Logger logger = LoggerFactory.getLogger(do_crossborder_communication.class);

    @Override
    public void execute(DelegateExecution execution) {
        JixelRabbitMQConsumerService jixelRabbitMQConsumerService = SpringContext.getBean(JixelRabbitMQConsumerService.class);
        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        if (nettunit.FailingTaskName.isPresent()) {
            if (nettunit.FailingTaskName.get().equals(this.getClass().getName())) {
                throw new BpmnError("REQUIRE_ORCHESTRATION",this.getClass().getName());
            }
        }

        logger.info("Executing capability ["+execution.getId()+"]: " + this.getClass().getSimpleName());

        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);
        String taskID = execution.getId();
        //jixelRabbitMQConsumerService.save(evt, taskID);
        //jixelRabbitMQConsumerService.save(evt, taskID);
        MUSAProducer.addRecipient(evt, JixelDomainInformation.ASP);
        MUSAProducer.addRecipient(evt, JixelDomainInformation.ARPA);

        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}