package nettunit.handler;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class evaluate_fire_radiant_energy implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String className = this.getClass().getSimpleName();
        execution.setVariable("hello",10);




        System.out.println("Executing capability: " + className);
        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}