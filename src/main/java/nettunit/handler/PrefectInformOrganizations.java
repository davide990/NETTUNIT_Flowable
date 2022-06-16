package nettunit.handler;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public class PrefectInformOrganizations  implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        /**/
        System.out.println("PREFET INFORME");
    }
}
