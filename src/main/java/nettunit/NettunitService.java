package nettunit;

import RabbitMQ.Consumer.JixelRabbitMQConsumer;
import RabbitMQ.JixelEvent;
import RabbitMQ.JixelEventUpdate;
import RabbitMQ.Listener.JixelConsumerListener;
import RabbitMQ.Producer.MUSARabbitMQProducer;
import RabbitMQ.Recipient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import nettunit.dto.InterventionRequest;
import nettunit.dto.ProcessInstanceResponse;
import nettunit.dto.ProcessInstancesRegister;
import nettunit.dto.TaskDetails;
import nettunit.persistence.PendingMessage;
import nettunit.persistence.PendingMessageRepository;
import nettunit.persistence.PendingMessagesService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Some;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NettunitService {

    public static final String GESTIONNAIRE_CANDIDATE_GROUP = "gestionnaire";
    public static final String POMPIERS_CANDIDATE_GROUP = "pompiers";
    public static final String PREFECTURE_CANDIDATE_GROUP = "prefecture";
    public static final String EQUIPE_INTERNE_CANDIDATE_GROUP = "equipe_interne";

    public static final String PROCESS_DEFINITION_KEY = "nettunitProcess_attentionPhase";

    RuntimeService runtimeService;
    TaskService taskService;
    ProcessEngine processEngine;
    RepositoryService repositoryService;

    // this is used to consume messages received from Jixel
    private static JixelRabbitMQConsumer consumer = new JixelRabbitMQConsumer();


    @Autowired
    private PendingMessagesService pendingMessagesService;

    {
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                consumer.init();
                consumer.startConsumerAndAwait(100, new Some<>(new JixelConsumerListener() {
                    @Override
                    public void onReceiveJixelEvent(JixelEvent event) {
                        completeTask(event);
                        //TODO verify task name.
                    }

                    @Override
                    public void onAddRecipient(Recipient r) {
                        completeTask(r);
                    }

                    @Override
                    public void onReceiveJixelEventUpdate(JixelEventUpdate update) {
                        completeTask(update);
                    }
                }));
            }

            private void completeTask(Object obj) {
                String pendingTaskID = pendingMessagesService.getTaskID(obj);
                if(!pendingTaskID.isEmpty()){
                    pendingMessagesService.remove(obj);
                    taskService.complete(pendingTaskID);
                    System.out.println("ACK");
                }
            }
        });
        t1.start();


    }

    private static Logger logger = LoggerFactory.getLogger(NettunitService.class);

    //********************************************************** deployment service methods **********************************************************

    public void deployProcessDefinition() {

        Deployment deployment =
                repositoryService
                        .createDeployment()
                        .addClasspathResource("AttentionPhase_complete.bpmn20.xml")
                        .deploy();
    }

    public List<ProcessDefinition> getProcessDefinitionsList() {
        return repositoryService.createProcessDefinitionQuery().list();
    }


    public ProcessInstanceResponse applyInterventionRequest(InterventionRequest interventionRequest) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("gestionnaire", interventionRequest.getEmpName());
        variables.put("description", interventionRequest.getRequestDescription());

        repositoryService.createProcessDefinitionQuery().list();

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);

        ProcessInstanceResponse pr = new ProcessInstanceResponse(processInstance.getId(), "begin", processInstance.isEnded());
        ProcessInstancesRegister.get().add(pr);
        //processRepo.save(pr);
        return pr;
        //return new ProcessInstanceResponse(processInstance.getId(), "begin", processInstance.isEnded());
    }

    /**
     * Retrieve the list of tasks that must be pursued by the specified role only
     *
     * @return
     */
    private List<TaskDetails> getTasksByRole(String role) {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(role).list();
        List<TaskDetails> taskDetails = getTaskDetails(tasks);

        return taskDetails;
    }

    public List<TaskDetails> getGestionnaireTasks() {
        return getTasksByRole(GESTIONNAIRE_CANDIDATE_GROUP);
    }

    public List<TaskDetails> getPompiersTasks() {
        return getTasksByRole(POMPIERS_CANDIDATE_GROUP);
    }

    public List<TaskDetails> getPrefectureTasks() {
        return getTasksByRole(PREFECTURE_CANDIDATE_GROUP);
    }

    public List<TaskDetails> getInternalEquipeTasks() {
        return getTasksByRole(EQUIPE_INTERNE_CANDIDATE_GROUP);
    }

    /**
     * Return the list of tasks that can be pursued (currently) for the specified process
     *
     * @param processID
     * @return
     */
    public List<TaskDetails> getTasks(String processID) {
        List<TaskDetails> taskDetails = new ArrayList<>();
        for (Task task : taskService.createTaskQuery().processInstanceId(processID).list()) {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            taskDetails.add(new TaskDetails(task.getId(), task.getName(), processID, processVariables));
        }
        return taskDetails;
    }


    private List<TaskDetails> getTaskDetails(List<Task> tasks) {
        List<TaskDetails> taskDetails = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            taskDetails.add(new TaskDetails(task.getId(), task.getName(), task.getProcessInstanceId(), processVariables));
        }
        return taskDetails;
    }

    /**
     * After the safety manager assesses the incident, requires the intervention of the competent bodies
     *
     * @param taskId
     * @param requireIntervention
     */
    public void requireIntervention(String taskId, Boolean requireIntervention) {
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("requireIntervention", requireIntervention.booleanValue());
        taskService.complete(taskId, variables);
    }

    public void makeAssessment(String taskId) {
        taskService.complete(taskId);
    }

    public void gestionnaire_confirmReceivedNotification(String taskId) {

        MUSARabbitMQProducer MUSA = new MUSARabbitMQProducer();
        JixelEvent fireInRefinery = new JixelEvent("INCENDIO IN RAFFINERIA", "INCENDIO");
        MUSA.notifyEvent(fireInRefinery);

//        repository.save(new PendingMessage(taskId, fireInRefinery));
        pendingMessagesService.save(fireInRefinery, taskId);
        //pendingMessages.put(fireInRefinery, taskId);

        //taskService.complete(taskId);
    }

    public void gestionnaire_activateInternalSecurityPlan(String taskId) {
        taskService.complete(taskId);
    }

    public void gestionnaire_selectPlanFromRepository(String taskId) {
        taskService.complete(taskId);
    }

    public void prefecture_receiveIncidentEvaluation(String taskId) {
        taskService.complete(taskId);
    }

    public void firefighter_receiveReport(String taskId) {
        taskService.complete(taskId);
    }

    public void firefighter_sendRescueTeam(String taskId) {
        taskService.complete(taskId);
    }
}
