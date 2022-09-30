package nettunit;

import JixelAPIInterface.Login.LoginToken;
import RabbitMQ.JixelEvent;
import Utils.JixelUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import nettunit.dto.InterventionRequest;
import nettunit.dto.TaskDetails;
import nettunit.persistence.NettunitTaskHistory;
import nettunit.rabbitMQ.ConsumerService.JixelRabbitMQConsumerService;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import org.apache.commons.io.IOUtils;
import org.flowable.engine.*;
import org.flowable.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentProperties;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import scala.Option;
import scala.xml.PrettyPrinter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardCopyOption;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NettunitService {

    public static final String GESTIONNAIRE_CANDIDATE_GROUP = "gestionnaire";
    public static final String POMPIERS_CANDIDATE_GROUP = "pompiers";
    public static final String PREFECTURE_CANDIDATE_GROUP = "prefecture";
    public static final String EQUIPE_INTERNE_CANDIDATE_GROUP = "equipe_interne";

    // ID of the process to be deployed
    public static final String PROCESS_DEFINITION_KEY = "nettunitProcess_attentionPhase";

    RuntimeService runtimeService;
    TaskService taskService;
    ProcessEngine processEngine;
    RepositoryService repositoryService;
    NettunitTaskHistory history;

    @Autowired
    private Environment env;

    /**
     * Service that handle the messages that are consumed by Jixel. Please note that this is necessary only for testing
     * purposes. Once the nettunit platform is deployed, this will not be used as this service will be available from
     * IES solution.
     */
    @Autowired
    private JixelRabbitMQConsumerService jixelRabbitMQConsumerService;


    /**
     * Service used to produce and send messages from MUSA to Jixel
     */
    @Autowired
    private MUSAProducerService MUSAProducer;

    private static Logger logger = LoggerFactory.getLogger(NettunitService.class);

    @PostConstruct
    private void postConstruct() {
        jixelRabbitMQConsumerService.setListener(taskID -> taskService.complete(taskID));
        processEngine.getProcessEngineConfiguration().setCreateDiagramOnDeploy(false);
    }

    /**
     * This method is invoked once the NETTUNIT platform is shut down.
     */
    @PreDestroy
    private void preDestroy() {
        Boolean deleteUnfinishedInstances =
                Boolean.parseBoolean(env.getProperty("nettunit.remove_unfinished_processes_on_terminate"));
        if (!deleteUnfinishedInstances) {
            return;
        }
        logger.info("~~~~~~SHUTTING DOWN~~~~~~\nAll unfinished process will be deleted");
        history.getUnfinishedProcessIDs()
                .forEach(pID -> runtimeService.deleteProcessInstance(pID, "[NETTUNIT] Unfinished process"));
    }

    //********************************************************** deployment service methods ****************************

    public void clearAllExistingDeployments() {
        List<String> previousDeploymentID = repositoryService.createDeploymentQuery().orderByDeploymentId().asc()
                .list().stream().map(x -> x.getId()).collect(Collectors.toList());
        previousDeploymentID.forEach(id -> repositoryService.deleteDeployment(id));
    }

    public void deployProcessDefinition() throws IOException {
        Deployment deployment =
                repositoryService
                        .createDeployment()
                        .addClasspathResource("AttentionPhase_complete.bpmn20.xml")
                        .deploy();
    }

    public void deployProcessDefinition(String processID, String processDef) {
        //Note: the deployment fails if the resource name does not finish with either ".bpmn" or ".bpmn20.xml"
        Deployment deployment =
                repositoryService
                        .createDeployment()
                        .addString(processID + ".bpmn", processDef) //MUST BE .BPMN
                        .deploy();

        int numDeployedArtifact = ((DeploymentEntityImpl) deployment).getDeployedArtifacts(ProcessDefinitionEntityImpl.class).size();
        if (numDeployedArtifact > 0) {
            logger.info("SUCCESS: process deployment with id " + processID);
        } else {
            logger.error("FAIL: process deployment with id " + processID);
        }
    }

    /**
     * Return the list of process definitions loaded into the current flowable instance
     *
     * @return
     */
    public List<ProcessDefinition> getProcessDefinitionsList() {
        return repositoryService.createProcessDefinitionQuery().list();
    }

    public List<ProcessInstance> getActiveProcessInstances(String processDefinitionID) {
        return runtimeService.createProcessInstanceQuery().active().list();
    }

    public List<ProcessDefinition> getActiveProcesses() {
        return repositoryService.createProcessDefinitionQuery().active().list();
    }

    /**
     * Entry point for the emergency plan. This operation creates a new instance of the emegency plan.
     * <p>
     * This is invoked when the system receives a new JixelEvent message from Jixel
     *
     * @param incidentEvent
     * @return
     */
    public void applyInterventionRequest(JixelEvent incidentEvent) {
        logger.info("~~~~~~~~~~~~~CREATING NEW PLAN INSTANCE~~~~~~~~~~~~~");
        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("id", incidentEvent.id());
        variables.put("event_type", incidentEvent.incident_type().description());

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);
    }


    /**
     * Entry point for the emergency plan. This operation creates a new instance of the emegency plan.
     * <p>
     * This is invoked when a new intervention request is inserted manually. This is for development purpose
     * only, as we suppose that plans are activated only when a new jixel event is received
     *
     * @param processInstanceRequest
     * @return
     */
    public void applyInterventionRequest(InterventionRequest processInstanceRequest) {
        logger.info("~~~~~~~~~~~~~CREATING NEW PLAN INSTANCE~~~~~~~~~~~~~");
        Map<String, Object> variables = new HashMap<>();
        variables.put("description", processInstanceRequest.getRequestDescription());

        // IMPORTANT
        // note that this is mandatory
        // I replaced the Tee symbol in sequence flow condition with the expression ${myVariable}.
        // If myVariable is unknown, the execution of the plan will fail.
        variables.put("myVariable", true);

        //Start the process
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(processInstanceRequest.getEmergencyID(), variables);
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

    /**
     * Return the process instance ID which the input task belongs to.
     *
     * @param taskID
     * @return a process ID
     */
    private String getProcessID(String taskID) {
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

    public void gestionnaire_confirmReceivedNotification(LoginToken loginToken, String taskId) {
        Option<JixelEvent> jixelEvent = JixelUtil.getJixelEvent(loginToken, 69);
        MUSAProducer.notifyEvent(jixelEvent.get());
        //Wait until Jixel consumes the message to complete the task
        jixelRabbitMQConsumerService.save(jixelEvent.get(), taskId);
        System.out.println("OK");
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
