package nettunit.handler;

import nettunit.NettunitService;
import nettunit.SpringContext;
import org.flowable.engine.delegate.BpmnError;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;

import java.util.Optional;

public abstract class MUSAJavaDelegate implements JavaDelegate {

    public boolean isFailingCapability(DelegateExecution execution, String classFullName) {
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        if (nettunit.FailingTaskName.isPresent()) {
            if (nettunit.FailingTaskName.get().equals(classFullName)) {
                String taskName = ((ExecutionEntityImpl) execution).getActivityName();
                String taskID = execution.getId();
                nettunit.FailedTaskName = Optional.of(taskName);
                nettunit.FailedTaskImplementation = Optional.of(this.getClass().getName());
                throw new BpmnError("REQUIRE_ORCHESTRATION",this.getClass().getName());
            }
        }
        return false;
    }

}
