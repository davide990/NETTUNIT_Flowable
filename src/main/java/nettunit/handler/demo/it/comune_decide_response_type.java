package nettunit.handler.demo.it;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.MUSA.StateOfWorldUpdateOp;
import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.handler.base.BaseHandler;
import nettunit.handler.do_crossborder_communication;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.mutable.ArrayBuffer;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class comune_decide_response_type extends BaseHandler implements TriggerableActivityBehavior {

    private static Logger logger = LoggerFactory.getLogger(comune_decide_response_type.class);

    String evolution_predicate = "decided_response_type(comune)";

    @Override
    public void trigger(DelegateExecution delegateExecution, String signalEvent, Object signalData) {
        this.getNETTUNITService().updateMUSAStateOfWorld(StateOfWorldUpdateOp.ADD, evolution_predicate, this.getClass().getName());
        logger.info("Capability executed correctly [" + delegateExecution.getId() + "]: " + this.getClass().getSimpleName());
    }

    @Override
    public void execute(DelegateExecution execution) {

        super.execute(execution);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());
        getNETTUNITService().currentTask = Optional.of(this.getClass().getName());

        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);

        String taskName = ((ExecutionEntityImpl) execution).getActivityName();
        String taskID = ((ExecutionEntityImpl) execution).getActivityId();

        this.getMUSAService().updateEventSeverity(evt, JixelDomainInformation.SEVERITY_LEVEL_STANDARD);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getMUSAService().updateEventDescription(evt, "test *presa in carico dal DRPC* [PCRS] + *interpretazione dei risultati del modello* [INM] + *valutazione del potenziale impatto sulla salute della popolazione interessata* [CNR-IFT] + *comunicazione attivazione dello stato di ALLARME, l&#39;attivazione del COC e del modello di intervento* [Prefetto] + *attivazione COC e delle sirene per l&#39;avviso alla popolazione di attuazione delle misure di auto-protezione&quot; [Sindaco_Pachino]");
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getNETTUNITService().currentTask = Optional.of(this.getClass().getName());
        this.getNETTUNITService().FailedTaskName = Optional.of(taskName);
        this.getNETTUNITService().FailedTaskImplementation = Optional.of(this.getClass().getName());

    }

}
