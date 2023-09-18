package nettunit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import nettunit.dto.InterventionRequest;
import nettunit.dto.ProcessInstanceDetail;
import nettunit.dto.TaskDetails;
import org.flowable.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used by the REST Web service. The methods in this class are invoked after a REST request from the
 * specified endpoint. The endpoint make reference to specific actions to carry out for solving the emergency plan
 * in BPMN format.
 */
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class NettunitController {
    private static Logger logger = LoggerFactory.getLogger(NettunitController.class);
    NettunitService nettunitService;

    //********************************************************** deployment endpoints **********************************************************
    @PostMapping("/NETTUNIT/clearDeployments")
    public void clearDeployments() {
        nettunitService.clearAllExistingDeployments();
    }

    @Deprecated
    @PostMapping("/NETTUNIT/deploy")
    public void deployWorkflow() throws IOException {
        nettunitService.deployProcessDefinition();
    }

    @PostMapping("/NETTUNIT/deployProcess/{processID}")
    public void deployWorkflow(@PathVariable("processID") String processID, @RequestBody String processDef) {
        nettunitService.deployProcessDefinition(processID, processDef);
    }

    @PostMapping("/NETTUNIT/removeProcessInstance/{processID}")
    public void removeProcessInstance(@PathVariable("processID") String processID) {
        nettunitService.removeProcessInstance(processID);
    }


    /**
     * This endpoint is used to tell this nettunit/flowable module the mapping between
     * service task names and their classes.
     *
     * @param processID
     * @param processDef
     */
    @PostMapping("/NETTUNIT/task_mapping/{processID}")
    public void receiveTaskMapping(@PathVariable("processID") String processID, @RequestBody String processDef) {
        //nettunitService.deployProcessDefinition(processID, processDef);
    }

    @GetMapping("/NETTUNIT/active_incident_list/{processDefinitionID}")
    public List<Map<String, Object>> getActiveProcessInstances(@PathVariable("processDefinitionID") String processDefinitionID) {
        List<Map<String, Object>> processInstanceMapping = new ArrayList<>();
        for (ProcessInstance pp : nettunitService.getActiveProcessInstances(processDefinitionID)) {
            Map<String, Object> map = new HashMap<>();
            for (String key : pp.getProcessVariables().keySet()) {
                map.put(key, pp.getProcessVariables().get(key));
            }
            map.put("callbackID", pp.getCallbackId());
            map.put("name", pp.getName());
            map.put("startTime", pp.getStartTime());
            map.put("processDefinitionName", pp.getProcessDefinitionName());
            map.put("processDefinitionId", pp.getProcessDefinitionId());
            map.put("processDefinitionKey", pp.getProcessDefinitionKey());
            map.put("processVariables", pp.getProcessVariables());
            map.put("isSuspended", pp.isSuspended());
            map.put("isEnded", pp.isEnded());
            map.put("processDefinitionVersion", pp.getProcessDefinitionVersion());
            map.put("description", pp.getDescription());
            processInstanceMapping.add(map);
        }
        return processInstanceMapping;
    }


    @GetMapping("/NETTUNIT/incident_list/")
    public List<String> getActiveProcesses() {
        return nettunitService.getActiveProcessesID();
    }

    @GetMapping("/NETTUNIT/incident_list_new/")
    public List<ProcessInstanceDetail> getActiveProcessesNew() {
        return nettunitService.getActiveProcesses();
    }

    @GetMapping("/NETTUNIT/terminated_incident_list/")
    public List<ProcessInstanceDetail> getTerminatedProcessesNew() {
        return nettunitService.getTerminatedProcesses();
    }

    /**
     * Simulate task failure
     *
     * @param taskName
     */
    @PostMapping("/NETTUNIT/fail/{taskName}")
    public void fail_task(@PathVariable("taskName") String taskName) {
        nettunitService.failTask(taskName);
    }

    @PostMapping("/NETTUNIT/undo_fail/{taskName}")
    public void undo_fail_task(@PathVariable("taskName") String taskName) {
        nettunitService.undoFailTask(taskName);
    }

    @GetMapping("/NETTUNIT/completed_tasks/{processID}")
    public List<TaskDetails> get_completed_tasks(@PathVariable("processID") String processID) {
        List<TaskDetails> allCompletedTasks = new ArrayList<>();
        if (nettunitService.completedUserTasksByEvents.containsKey(processID)) {
            allCompletedTasks.addAll(nettunitService.completedUserTasksByEvents.get(processID));
        }
        if (nettunitService.completedServiceTasksByEvents.containsKey(processID)) {
            allCompletedTasks.addAll(nettunitService.completedServiceTasksByEvents.get(processID));
        }

        return allCompletedTasks;
        //return nettunitService.completedUserTasksByEvents.get(processID);
    }


    @PostMapping("/NETTUNIT/get_diagram/")
    public byte[] get_diagram(@RequestBody ProcessInstanceDetail prInstance) throws IOException {

        BufferedImage diagram = nettunitService.getDiagramImage(prInstance);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(diagram, "png", baos);
        return baos.toByteArray();
    }

    @PostMapping("/NETTUNIT/incident/apply")
    public void applyInterventionRequest(@RequestBody InterventionRequest interventionRequest) {
        nettunitService.applyInterventionRequest(interventionRequest);
    }

    @GetMapping("/NETTUNIT/task_list/{processID}")
    public List<TaskDetails> getAllTasks(@PathVariable("processID") String processID) {
        return nettunitService.getTasks(processID);
    }

    @GetMapping("/NETTUNIT/pending_messages/{taskID}")
    public int getPendingMessagesCount(@PathVariable("taskID") String taskID) {
        return 0;//nettunitService.getPendingMessagesCount(taskID);
    }


}
