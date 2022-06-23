package nettunit.handler;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MUSAOrchestrationHandler implements JavaDelegate {
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(MUSAOrchestrationHandler.class);

    @Override
    public void execute(DelegateExecution execution) {
        String myName = execution.getCurrentFlowElement().getName();
        String myID = execution.getCurrentFlowElement().getId();

        logger.info("~~~~~~ ~~~~~~~~~~~~~~~~~~ ~~~~~~");
        logger.info("[" + DATE_FORMATTER.format(new Date()) + "] Sent adaptivity request from . ");
        logger.info("~~~~~~ ~~~~~~~~~~~~~~~~~~ ~~~~~~");
    }
}
