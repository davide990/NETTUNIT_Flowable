package nettunit.listener;

import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.dto.TaskDetails;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TaskEndedExecutionListenerImpl implements ExecutionListener {
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(TaskEndedExecutionListenerImpl.class);

    @Override
    public void notify(DelegateExecution execution) {
        String myName = execution.getCurrentFlowElement().getName();
        String myID = execution.getId();
        //MUSAProducerService MUSAProducer = SpringContext.getBean(MUSAProducerService.class);
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);

        String taskID = execution.getId();
        String processID = execution.getProcessInstanceId();
        String taskName = ((ExecutionEntityImpl) execution).getActivityName();

        if (!nettunit.completedServiceTasksByEvents.containsKey(processID)) {
            nettunit.completedServiceTasksByEvents.put(processID, new ArrayList<>());
        }
        nettunit.completedServiceTasksByEvents.get(processID).add(new TaskDetails(taskID,
                taskName,
                processID,
                new HashMap<>()));

        logger.info("[" + DATE_FORMATTER.format(new Date()) + "]  ended task: \"" + myName + "\" with ID: " + myID);
    }
}

