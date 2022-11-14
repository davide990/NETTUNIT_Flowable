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

public class inform_technical_rescue_organisation_internal_plan implements JavaDelegate {

    /**
     * Service that handle the messages that are consumed by Jixel. Please note that this is necessary only for testing
     * purposes. Once the nettunit platform is deployed, this will not be used as this service will be available from
     * IES solution.
     */
    //@Autowired
    //private JixelRabbitMQConsumerService jixelRabbitMQConsumerService;

    //@Autowired
    //private MUSAProducerService MUSAProducer;

    private static Logger logger = LoggerFactory.getLogger(inform_technical_rescue_organisation_internal_plan.class);

    @Override
    public void execute(DelegateExecution execution) {

        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        if (nettunit.FailingTaskName.isPresent()) {
            if (nettunit.FailingTaskName.get().equals(this.getClass().getName())) {
                String taskName = ((ExecutionEntityImpl) execution).getActivityName();
                nettunit.FailedTaskName = Optional.of(taskName);
                nettunit.FailedTaskImplementation = Optional.of(this.getClass().getName());
                throw new BpmnError("REQUIRE_ORCHESTRATION", this.getClass().getName());
            }
        }
        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());
        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.ASP);
        recipients.addOne(JixelDomainInformation.ARPA);
        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);
        MUSAProducer.addRecipient(evt, recipients.toList());
    }
}