package nettunit.handler_old;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class MonitorIncidentHandler implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {

        // if too much time passes, then throw error

        System.out.println("SENT TO MUSA");
    }
}
