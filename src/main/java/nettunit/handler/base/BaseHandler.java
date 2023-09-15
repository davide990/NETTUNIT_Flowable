package nettunit.handler.base;

import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.rabbitMQ.ConsumerService.MUSARabbitMQConsumerService;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;

import java.util.Optional;

public abstract class BaseHandler implements JavaDelegate {

    private MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);

    private NettunitService nettunit = SpringContext.getBean(NettunitService.class);

    private MUSARabbitMQConsumerService musaRabbitMQConsumerService = SpringContext.getBean(MUSARabbitMQConsumerService.class);

    /*
     * Public
     */

    public void execute(DelegateExecution execution) {
        errorHandling(execution);
    }

    public MUSAProducerService getMUSAService() {
        return this.MUSAProducer;
    }

    public NettunitService getNETTUNITService() {
        return this.nettunit;
    }

    public MUSARabbitMQConsumerService getMusaRabbitMQConsumerService(){
        return this.musaRabbitMQConsumerService;
    }

    /*
     * Protected
     */

    protected void errorHandling(DelegateExecution execution) {

        NettunitService nettunitService = getNETTUNITService();

        if (nettunitService.FailingTaskName.isPresent()) {
            if (nettunitService.FailingTaskName.get().equals(this.getClass().getName())) {
                String taskName = ((ExecutionEntityImpl) execution).getActivityName();
                String taskID = execution.getId();
                nettunitService.FailedTaskName = Optional.of(taskName);
                nettunitService.FailedTaskImplementation = Optional.of(this.getClass().getName());
                throw new BpmnError("REQUIRE_ORCHESTRATION", this.getClass().getName());
            }
        }

    }

}
