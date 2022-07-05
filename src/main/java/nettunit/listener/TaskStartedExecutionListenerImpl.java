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


    /**
     * Return the process instance ID which the input task belongs to.
     *
     * @param taskID
     * @return a process ID
     */
    private String getProcessID(String taskID) {
        TaskService taskService = SpringContext.getBean(TaskService.class);

        //get the tasks
        List<Task> tasks = taskService.createTaskQuery().list();
        //get the one which task id matches the input ID
        Optional<Task> tt = tasks.stream().filter(t -> t.getId().equals(taskID)).findAny();
        if (tt.isPresent()) {
            //return the corresponding process ID
            return tt.get().getProcessInstanceId();
        }
        throw new InvalidParameterException("No task found for specified task ID.");
    }


    @Override
    public void notify(DelegateExecution execution) {
        String myName = execution.getCurrentFlowElement().getName();
        String myID = execution.getId();

        TaskListenerService taskListenerService = SpringContext.getBean(TaskListenerService.class);
        taskListenerService.myFun();

        logger.info("[" + DATE_FORMATTER.format(new Date()) + "]  started/ended task: \"" + myName + "\" with ID: " + myID);
    }


}


