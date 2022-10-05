package nettunit.handler;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class keep_update_involved_personnel implements JavaDelegate {

    private static Logger logger = LoggerFactory.getLogger(keep_update_involved_personnel.class);

    @Override
    public void execute(DelegateExecution execution) {
        String className = this.getClass().getSimpleName();
        execution.setVariable("hello", 10);

        logger.info("Executing capability: " + className);
        //throw new BpmnError("REQUIRE_ORCHESTRATION");
    }
}