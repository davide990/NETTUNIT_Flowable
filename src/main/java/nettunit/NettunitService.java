package nettunit;

import RabbitMQ.JixelEvent;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import nettunit.MUSA.StateOfWorldUpdateOp;
import nettunit.dto.InterventionRequest;
import nettunit.dto.ProcessInstanceDetail;
import nettunit.dto.TaskDetails;
import nettunit.persistence.NettunitTaskHistory;
import nettunit.rabbitMQ.ConsumerService.MUSARabbitMQConsumerService;
import nettunit.rabbitMQ.PendingMessageComponentListener;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import nettunit.util.BPMNToImage;
import okhttp3.*;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.persistence.entity.DeploymentEntityImpl;
import org.flowable.engine.impl.persistence.entity.HistoricProcessInstanceEntityImpl;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntityImpl;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import scala.collection.mutable.ArrayBuffer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NettunitService {
    public static final String JIXEL_EVENT_VAR_NAME = "JixelEvent";

    /**
     * Contains the name of the current task. This is used to trace the execution and, in case of
     * adaptation, this value is sent to MUSA in case of an adaptivity request.
     */
    public Optional<String> currentTask;

    /**
     * This is the name of task that *will* faill
     */
    public Optional<String> FailingTaskName;

    /**
     * Once FailingTaskName has failed, his name is transfered here. BPMN diagram layout listen to this field when
     * coloring the failed task in red
     */
    public Optional<String> FailedTaskName;
    public Optional<String> FailedTaskImplementation;

    // ID of the process to be deployed
    public static final String PROCESS_DEFINITION_KEY = "emg_it";//"NETTUNITProcess";
    RuntimeService runtimeService;
    TaskService taskService;
    ProcessEngine processEngine;
    RepositoryService repositoryService;
    NettunitTaskHistory history;

    HistoryService historyService;

    ManagementService managementService;


    @Autowired
    private Environment env;

    /**
     * Service that handle the messages that are consumed by Jixel. Please note that this is necessary only for testing
     * purposes. Once the nettunit platform is deployed, this will not be used as this service will be available from
     * IES solution.
     */
    //@Autowired
    //private JixelRabbitMQConsumerService jixelRabbitMQConsumerService;

    @Autowired
    private MUSARabbitMQConsumerService MUSARabbitMQConsumerService;

    /**
     * This map associates the incidental events to process IDs.
     */
    private Map<JixelEvent, String> processByEvents;

    //key -> process instance ID, obtained by getProcessID(taskID)
    public Map<JixelEvent, List<TaskDetails>> completedUserTasksByEvents;

    public Map<String, List<TaskDetails>> completedServiceTasksByEvents;


    /**
     * Service used to produce and send messages from MUSA to Jixel
     */
    @Autowired
    private MUSAProducerService MUSAProducer;

    //@Autowired
    //private JixelProducerService JixelProducer;

    private static final Logger logger = LoggerFactory.getLogger(NettunitService.class);

    private static boolean deployment = false;

    @PostConstruct
    private void postConstruct() {
        processByEvents = new HashMap<>();
        completedUserTasksByEvents = new HashMap<>();
        completedServiceTasksByEvents = new HashMap<>();


        processEngine.getProcessEngineConfiguration().setCreateDiagramOnDeploy(false);
        processEngine.getProcessEngineConfiguration().setAsyncExecutorActivate(true);
        processEngine.getProcessEngineConfiguration().setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);

        deployment = Boolean.parseBoolean(env.getProperty("deployment_flag"));

        //asyncExecutorActivate

        if (deployment)
            MUSARabbitMQConsumerService.setListener(new PendingMessageComponentListener() {
                @Override
                public void completeTask(JixelEvent evt, String taskID) {
                    onCompleteTask(evt, taskID);
                }

                @Override
                public void applyInterventionRequest(JixelEvent evt) {
                    NettunitService nettunit = SpringContext.getBean(NettunitService.class);
                    nettunit.applyInterventionRequest(evt);
                }
            });
        //else
        /*JixelProducer.setListener(new PendingMessageComponentListener() {
            @Override
            public void completeTask(JixelEvent evt, String taskID) {
                onCompleteTask(evt, taskID);
            }

            @Override
            public void applyInterventionRequest(JixelEvent evt) {
                NettunitService nettunit = SpringContext.getBean(NettunitService.class);
                nettunit.applyInterventionRequest(evt);
            }
        });*/
    }

    private void onCompleteTask(JixelEvent evt, String taskID) {
        // I set a variable so that I can access the event (at the current state) from service task handlers
        //runtimeService.setVariable(getProcessID(taskID), JIXEL_EVENT_VAR_NAME, evt);

        if (evt.incident_id().isEmpty()){
            runtimeService.setVariable(getProcessID(taskID), JIXEL_EVENT_VAR_NAME, evt);
            processByEvents.put(evt, getProcessID(taskID));
        }


        // update the event
        processByEvents.put(evt, getProcessID(taskID));
        // complete the task. Check => taskService.createTaskQuery().taskUnassigned().list()
        //taskService.complete(taskID);
        completeUserTask(evt, taskID);
        logger.info("Completed Task with ID: " + taskID);
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
        repositoryService
                .createDeployment()
                .addClasspathResource("AttentionPhase_complete.bpmn20.xml")
                .deploy();
    }

    /**
     * Communicate to musa that
     *
     * @param opType
     * @param predicate
     * @param taskFullName
     */
    public void updateMUSAStateOfWorld(StateOfWorldUpdateOp opType, String predicate, String taskFullName) {
        String MUSAAddress = getEnvironment().getProperty("nettunit.musa.address");
        String MUSAPort = getEnvironment().getProperty("nettunit.musa.port");

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");

        String theString = opType + "|" + predicate + "|" + taskFullName;

        RequestBody body = RequestBody.create(theString, mediaType);
        Request request = new Request.Builder()
                .url("http://" + MUSAAddress + ":" + MUSAPort + "/UpdateStateOfWorld")
                .method("POST", body)
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * This is used when deployment and execution are invoked from MUSA
     *
     * @param processID
     * @param processDef
     */
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
     * This is invoked when only the BPMN definition of the emergency plan is provided
     *
     * @param processDef
     */
    public void deployProcessDefinition(String processDef) {
        //Note: the deployment fails if the resource name does not finish with either ".bpmn" or ".bpmn20.xml"
        Deployment deployment =
                repositoryService
                        .createDeployment()
                        .addString(PROCESS_DEFINITION_KEY + ".bpmn", processDef) //MUST BE .BPMN
                        .deploy();

        int numDeployedArtifact = ((DeploymentEntityImpl) deployment).getDeployedArtifacts(ProcessDefinitionEntityImpl.class).size();
        if (numDeployedArtifact > 0) {
            logger.info("SUCCESS: process deployment with id " + PROCESS_DEFINITION_KEY);
        } else {
            logger.error("FAIL: process deployment with id " + PROCESS_DEFINITION_KEY);
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

    /**
     * Returns the list of process instances which ID is that provided in input
     *
     * @param processDefinitionID
     * @return
     */
    public List<ProcessInstance> getActiveProcessInstances(String processDefinitionID) {
        return runtimeService.createProcessInstanceQuery().active().list();
    }

    /**
     * @return
     */
    public List<ProcessInstanceDetail> getActiveProcesses() {
        List<ProcessInstance> inst = runtimeService.createProcessInstanceQuery().active().list();

        return inst.stream().map(i ->
                        new ProcessInstanceDetail(i.getProcessDefinitionKey(),
                                i.getProcessInstanceId(),
                                i.getProcessDefinitionName(),
                                i.getProcessDefinitionVersion()))
                .collect(Collectors.toList());
    }

    public List<ProcessInstanceDetail> getTerminatedProcesses() {
        List<HistoricProcessInstance> inst = historyService.createHistoricProcessInstanceQuery().finished().list();
        return inst.stream()
                .map(i -> (HistoricProcessInstanceEntityImpl) i)
                .map(is -> new ProcessInstanceDetail(Optional.ofNullable(is.getProcessDefinitionKey()).orElse(""),
                        Optional.ofNullable(is.getProcessInstanceId()).orElse(""),
                        Optional.ofNullable(is.getProcessDefinitionName()).orElse(""),
                        Optional.ofNullable(is.getProcessDefinitionVersion()).orElse(-1)))
                .filter(ii -> ii.getProcessDefinitionName().equals("CrossBorderEmergencyPlan"))
                .collect(Collectors.toList());
    }


    public List<String> getActiveProcessesID() {
        List<Task> tasks = taskService.createTaskQuery().taskUnassigned().list();
        List<String> processIds = new ArrayList<>();
        for (Task t : tasks) {
            processIds.add(t.getProcessInstanceId());
        }
        return processIds;
    }

    public void applyInterventionRequest(JixelEvent incidentEvent) {
        applyInterventionRequest(incidentEvent, PROCESS_DEFINITION_KEY);
    }

    /**
     * Entry point for the emergency plan. This operation creates a new instance of the emegency plan.
     * <p>
     * This is invoked when the system receives a new JixelEvent message from Jixel
     *
     * @param incidentEvent
     * @return
     */
    public void applyInterventionRequest(JixelEvent incidentEvent, String processDefinitionKey) {
        logger.info("~~~~~~~~~~~~~CREATING NEW PLAN INSTANCE (event from JIXEL)~~~~~~~~~~~~~");
        logger.info(" ~~~~~~ Jixel event id: " + incidentEvent.id());
        logger.info(" ~~~~~~ Jixel caller: " + incidentEvent.caller_name());
        logger.info(" ~~~~~~ Process instance: " + processDefinitionKey);
        Map<String, Object> variables = new HashMap<String, Object>();

        int id = incidentEvent.id();
        String evt_type = incidentEvent.description();
        String caller_name = "";
        if (incidentEvent.caller_name().isDefined()) {
            caller_name = incidentEvent.caller_name().get();
        }

        variables.put("id", id);
        variables.put("event_type", evt_type);
        variables.put("caller_name", caller_name);
        variables.put(JIXEL_EVENT_VAR_NAME, incidentEvent);

        // IMPORTANT
        // note that this is mandatory
        // I replaced the Tee symbol in sequence flow condition with the expression ${myVariable}.
        // If myVariable is unknown, the execution of the plan will fail.
        variables.put("myVariable", true);

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);

        //Store the process ID and the event
        processByEvents.put(incidentEvent, processInstance.getProcessInstanceId());
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
        logger.info("Request: " + processInstanceRequest);
        Map<String, Object> variables = new HashMap<>();
        variables.put("description", processInstanceRequest.getRequestDescription());

        // IMPORTANT
        // note that this is mandatory
        // I replaced the Tee symbol in sequence flow condition with the expression ${myVariable}.
        // If myVariable is unknown, the execution of the plan will fail.
        variables.put("myVariable", true);

        //Start the process
        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(processInstanceRequest.getEmergencyPlanID(), variables);
    }

    /**
     * Retrieve the list of tasks that must be pursued by the specified role only
     *
     * @return
     */
    private List<TaskDetails> getTasksByRole(String role) {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(role).list();
        return getTaskDetails(tasks);
    }

    /**
     * Return the process instance ID which the input task belongs to.
     *
     * @param taskID
     * @return a process ID
     */
    public String getProcessID(String taskID) {
        //get the tasks
        List<Task> tasks = taskService.createTaskQuery().list();

        Optional<ActivityInstance> ee = runtimeService.createActivityInstanceQuery().list().stream()
                .filter(t -> t.getActivityId().equals(taskID)).findFirst();


        //get the one which task id matches the input ID
        Optional<Task> tt = tasks.stream().filter(t -> t.getId().equals(taskID)).findAny();
        if (ee.isPresent()) {  //if (tt.isPresent()) {
            //return the corresponding process ID
            return ee.get().getProcessInstanceId();
        } else if (tt.isPresent()) {
            return tt.get().getProcessInstanceId();
        }
        logger.error("No process found from task id [\"" + taskID + "\"]");
        throw new InvalidParameterException("No task found with id [" + taskID + "]");
    }

    private String getTaskName(String taskID) {
        //get the tasks
        List<Task> tasks = taskService.createTaskQuery().list();
        Optional<ActivityInstance> ee = runtimeService.createActivityInstanceQuery().list().stream()
                .filter(t -> t.getActivityId().equals(taskID)).findFirst();

        //get the one which task id matches the input ID
        Optional<Task> tt = tasks.stream().filter(t -> t.getId().equals(taskID)).findAny();
        if (ee.isPresent()) {
            return ee.get().getActivityName();//.getName();
        } else if (tt.isPresent()) {
            return tt.get().getName();
        }
        logger.error("No task found with id [\"" + taskID + "\"]");
        throw new InvalidParameterException("No task found with id [" + taskID + "]");
    }

    /**
     * Return the list of tasks that can be pursued (currently) for the specified process
     *
     * @param processID
     * @return
     */
    public List<TaskDetails> getTasks(String processID) {
        List<TaskDetails> taskDetails = new ArrayList<>();
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processID).list();
        for (Task task : taskList) {
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

    public void send_team_to_evaluate(String taskID) {
        //get the ID of the process which the input task belongs to
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            logger.error("No event found for task [" + taskID + "]");
            return;
        }
        if (deployment) {
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
        }
        ArrayBuffer recipients = new ArrayBuffer<>();

        recipients.addOne(JixelDomainInformation.MAYOR);
        MUSAProducer.addRecipient(evt.get(), recipients.toList());
    }

    public void activate_internal_security_plan(String taskID) {
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            logger.error("No event found for task [" + taskID + "]");
            return;
        }
        if (deployment) {
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
        }
        MUSAProducer.updateEventSeverity(evt.get(), JixelDomainInformation.SEVERITY_LEVEL_STANDARD);
        MUSAProducer.updateEventDescription(evt.get(), "Internal Plan Activated");
        //completeUserTask(taskID);
    }

    public void decide_response_type(String taskID) {
        //get the ID of the process which the input task belongs to
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            logger.error("No event found for task [" + taskID + "]");
            return;
        }
        //Wait until Jixel consumes the message to complete the task
        if (deployment) {
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
        }
        MUSAProducer.updateUrgencyLevel(evt.get(), JixelDomainInformation.URGENCY_LEVEL_IMMEDIATA);
        MUSAProducer.updateEventSeverity(evt.get(), JixelDomainInformation.SEVERITY_LEVEL_STANDARD);
        MUSAProducer.updateEventDescription(evt.get(), "my description");


        //runtimeService.deleteProcessInstance();
    }

    public void declare_pre_alert_state(String taskID) {
        //get the ID of the process which the input task belongs to
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            logger.error("No event found for task [" + taskID + "]");
            return;
        }
        //Wait until Jixel consumes the message to complete the task
        if (deployment) {
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
        }
        MUSAProducer.updateUrgencyLevel(evt.get(), JixelDomainInformation.URGENCY_LEVEL_IMMEDIATA);
    }

    public void evaluate_fire_radiant_energy(String taskID) {
        //get the ID of the process which the input task belongs to
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            logger.error("No event found for task [" + taskID + "]");
            return;
        }
        if (deployment) {
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
        }
        MUSAProducer.updateEventDescription(evt.get(), "Fire radiant energy evaluated");
        //completeUserTask(taskID);
    }

    public void declare_alarm_state(String taskID) {
        //get the ID of the process which the input task belongs to
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            logger.error("No event found for task [" + taskID + "]");
            return;
        }
        if (deployment) {
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
        }
        MUSAProducer.updateEventDescription(evt.get(), "Alarm state declared");
        MUSAProducer.updateUrgencyLevel(evt.get(), JixelDomainInformation.URGENCY_LEVEL_FUTURA);
        //completeUserTask(taskID);
    }

    public void completeUserTask(JixelEvent evt, String taskID) {
        if (!completedUserTasksByEvents.containsKey(evt)) {
            completedUserTasksByEvents.put(evt, new ArrayList<>());
        }
        completedUserTasksByEvents.get(evt).add(new TaskDetails(taskID,
                getTaskName(taskID),
                getProcessID(taskID),
                new HashMap<>()));


        // in your case only one execution is selected
        List<Execution> executions = runtimeService.createExecutionQuery().onlyChildExecutions()
                .processInstanceId(processByEvents.get(evt)).list();
// activityId is id from the modeler. Can be any wait state (user task, async service task, receive task.....)
         List<String> activityIds = executions.stream().map(Execution::getActivityId).collect(Collectors.toList());


        Optional<Execution> execution = Optional.ofNullable(this.runtimeService.createExecutionQuery()
                .processInstanceId(processByEvents.get(evt))
                .activityId(taskID)
                .singleResult());

        Task task = this.taskService.createTaskQuery()
                .processInstanceId(processByEvents.get(evt))
                .taskId(taskID)
                .singleResult();

        if (execution.isPresent()) {
            this.runtimeService.trigger(execution.get().getId());
        } else {
            logger.error("No execution found for taskID: " + taskID);
        }
        //                .activityId(getTaskName(taskID))
        /*Task task = this.taskService.createTaskQuery()
                .processInstanceId(processByEvents.get(evt))
                //.taskName(getTaskName(taskID))
                .taskId(execution.getId())
                .singleResult();*/

        // Trigger the service task.
        //this.runtimeService.trigger(execution.getId());




        // Complete the user task
        //this.taskService.complete(task.getId());
    }


    public void failTask(String taskName) {
        logger.warn("Request failure for task [" + taskName + "]");
        FailingTaskName = Optional.of(taskName);
    }

    public void undoFailTask(String taskName) {
        logger.warn("Request undo failure for task [" + taskName + "]");
        FailingTaskName = Optional.empty();
        FailedTaskName = Optional.empty();
    }

    public BufferedImage getDiagramImage(ProcessInstanceDetail details) {
        String defName = details.getProcessDefinitionName();
        int ver = details.getProcessDefinitionVersion();
        ProcessDefinition process = repositoryService.createProcessDefinitionQuery()
                .processDefinitionName(defName)
                .processDefinitionVersion(ver)
                .singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(process.getId());
        return BPMNToImage.getBPMNDiagramImage(bpmnModel, details);
    }

    public void removeProcessInstance(String processID) {
        runtimeService.deleteProcessInstance(processID, "REMOVED BY USER");
    }


    public Environment getEnvironment() {
        return env;
    }

    public int getPendingMessagesCount(String taskID) {
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            return 0;
        }
        return MUSARabbitMQConsumerService.getNumberOfPendingMessages(evt.get().id());
    }

    public void inform_involved_local_authorities(String taskID) {
        //get the ID of the process which the input task belongs to
        String processID = getProcessID(taskID);
        Optional<JixelEvent> evt = Optional.empty();//Optional.ofNullable(processByEvents.get(processID));
        if (evt.isEmpty()) {
            logger.error("No event found for task [" + taskID + "]");
            return;
        }
        if (deployment) {
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
            MUSARabbitMQConsumerService.save(evt.get(), taskID);
        }

        MUSAProducer.updateEventDescription(evt.get(), "inform_involved_local_authorities");
        MUSAProducer.updateUrgencyLevel(evt.get(), JixelDomainInformation.URGENCY_LEVEL_FUTURA);
        //completeUserTask(taskID);
    }

    public ManagementService getManagementService() {
        return managementService;
    }
}
