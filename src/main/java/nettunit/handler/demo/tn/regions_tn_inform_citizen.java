package nettunit.handler.demo.tn;

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

public class regions_tn_inform_citizen extends BaseHandler {

    private static Logger logger = LoggerFactory.getLogger(regions_tn_inform_citizen.class);

    @Override
    public void execute(DelegateExecution execution) {

        super.execute(execution);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());
        getNETTUNITService().currentTask = Optional.of(this.getClass().getName());

        //TODO
        // send to MUSA predicate update (ex. obtained_health_risk_estimate >> evolution)

        MUSAProducerService musaService = getMUSAService();
        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);

        musaService.updateEventDescription(evt, "*attivazione COC e delle sirene per l'avviso alla popolazione di attuazione delle misure di autoprotezione* [Sindaco_Pachino]");

    }

}
