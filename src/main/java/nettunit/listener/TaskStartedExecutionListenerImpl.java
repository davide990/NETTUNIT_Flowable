package nettunit.listener;

import nettunit.SpringContext;
import nettunit.taskService.TaskListenerService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TaskStartedExecutionListenerImpl implements ExecutionListener {
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(TaskStartedExecutionListenerImpl.class);

    @Override
    public void notify(DelegateExecution execution) {
        String myName = execution.getCurrentFlowElement().getName();
        String myID = execution.getCurrentFlowElement().getId();

        TaskListenerService taskListenerService = SpringContext.getBean(TaskListenerService.class);
        taskListenerService.myFun();

        logger.info("[" + DATE_FORMATTER.format(new Date()) + "]  started/ended task: \"" + myName + "\" with ID: " + myID);
    }
}


