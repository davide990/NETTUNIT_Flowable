package nettunit.listener;

import nettunit.SpringContext;
import nettunit.taskService.TaskListenerService;
import org.flowable.engine.TaskService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class TaskStartedExecutionListenerImpl implements ExecutionListener {
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(TaskStartedExecutionListenerImpl.class);

    @Override
    public void notify(DelegateExecution execution) {
        String myName = execution.getCurrentFlowElement().getName();
        String myID = execution.getId();

        //TaskListenerService taskListenerService = SpringContext.getBean(TaskListenerService.class);
        //taskListenerService.myFun();

        logger.info("[" + DATE_FORMATTER.format(new Date()) + "]  started task: \"" + myName + "\" with ID: " + myID);
    }


}


