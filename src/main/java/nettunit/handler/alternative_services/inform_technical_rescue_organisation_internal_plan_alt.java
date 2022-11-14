package nettunit.handler.alternative_services;

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
import scala.collection.mutable.ArrayBuffer;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class inform_technical_rescue_organisation_internal_plan_alt implements JavaDelegate {

    /**
     * Service that handle the messages that are consumed by Jixel. Please note that this is necessary only for testing
     * purposes. Once the nettunit platform is deployed, this will not be used as this service will be available from
     * IES solution.
     */
    //@Autowired
    //private JixelRabbitMQConsumerService jixelRabbitMQConsumerService;

    //@Autowired
    //private MUSAProducerService MUSAProducer;

    private static Logger logger = LoggerFactory.getLogger(inform_technical_rescue_organisation_internal_plan_alt.class);

    @Override
    public void execute(DelegateExecution execution) {

        MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        if (nettunit.FailingTaskName.isPresent()) {
            if (nettunit.FailingTaskName.get().equals(this.getClass().getName())) {
                throw new BpmnError("REQUIRE_ORCHESTRATION",this.getClass().getName());
            }
        }
        logger.info("Executing capability: " + this.getClass().getSimpleName());


        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);
        //String taskID = execution.getId();
        //jixelRabbitMQConsumerService.save(evt, taskID);
        //jixelRabbitMQConsumerService.save(evt, taskID);

        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.ASP);
        recipients.addOne(JixelDomainInformation.ARPA);
        MUSAProducer.addRecipient(evt, recipients.toList());

        //MUSAProducer.addRecipient(evt, JixelDomainInformation.ASP);
        //MUSAProducer.addRecipient(evt, JixelDomainInformation.ARPA);

        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}