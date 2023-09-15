package nettunit.handler.demo.it;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.handler.base.BaseHandler;
import nettunit.handler.do_crossborder_communication;
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

public class comune_involve_competent_roles extends BaseHandler {

    private static Logger logger = LoggerFactory.getLogger(comune_involve_competent_roles.class);

    @Override
    public void execute(DelegateExecution execution) {

        super.execute(execution);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());
        getNETTUNITService().currentTask = Optional.of(this.getClass().getName());

        //TODO
        // send to MUSA predicate update (ex. obtained_health_risk_estimate >> evolution)

        MUSAProducerService musaService = getMUSAService();
        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);

        String taskName = ((ExecutionEntityImpl) execution).getActivityName();
        String taskID = ((ExecutionEntityImpl) execution).getActivityId();
        this.getNETTUNITService().currentTask = Optional.of(this.getClass().getName());
        this.getNETTUNITService().FailedTaskName = Optional.of(taskName);
        this.getNETTUNITService().FailedTaskImplementation = Optional.of(this.getClass().getName());
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.DIRECTOR_ETNEA_OBSERVATORY);
        recipients.addOne(JixelDomainInformation.PCRS);
        recipients.addOne(JixelDomainInformation.REGIONE_SICILIA);
        recipients.addOne(JixelDomainInformation.COMUNE_PACHINO_POLIZIA_MUNICIPALE);
        musaService.addRecipient(evt, recipients.toList());

        musaService.updateCommType(evt, JixelDomainInformation.COMM_TYPE_OPERATIVA);
        musaService.updateEventTypology(evt, JixelDomainInformation.EVENT_TYPE_VULCANO);
        musaService.updateEventSeverity(evt, JixelDomainInformation.SEVERITY_LEVEL_STANDARD);
        musaService.updateEventDescription(evt, "test *presa in carico dal DRPC* [PCRS]");

    }

}
