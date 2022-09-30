package nettunit.handler;

import RabbitMQ.JixelEvent;
import nettunit.rabbitMQ.ConsumerService.JixelRabbitMQConsumerService;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;

public class activate_internal_security_plan implements JavaDelegate {

    @Autowired
    private MUSAProducerService MUSAProducer;

    @Override
    public void execute(DelegateExecution execution) {
        String className = this.getClass().getSimpleName();
        execution.setVariable("hello",10);

        //JixelEvent ev = new JixelEvent()
        //MUSAProducer.notifyEvent(ev)

        System.out.println("Executing capability: " + className);
        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}