package nettunit.handler;

import lombok.Data;

import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

@Data
public class CreateNettunitEventHandler implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        System.out.println("CREATED NETTUNIT EVENT");

        //String currentProcessID = ((ExecutionEntityImpl) execution).getProcessInstance().getId();
        //ProcessInstancesRegister.get().setStatus(currentProcessID,"FAILED");

        throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}
