package nettunit.handler;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.handler.base.BaseHandler;
import org.flowable.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.mutable.ArrayBuffer;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class do_crossborder_communication extends BaseHandler {

    private static Logger logger = LoggerFactory.getLogger(do_crossborder_communication.class);

    @Override
    public void execute(DelegateExecution execution) {

        super.execute(execution);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());

        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);

        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.ASP);
        recipients.addOne(JixelDomainInformation.ARPA);

        getMUSAService().addRecipient(evt, recipients.toList());


        //TODO
        // send to MUSA predicate update (ex. obtained_health_risk_estimate >> evolution)

    }

}