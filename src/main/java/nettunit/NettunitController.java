package nettunit;

import JixelAPIInterface.Login.LoginToken;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import nettunit.dto.InterventionRequest;
import nettunit.dto.TaskDetails;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used in REST Web service. The methods in this class are invoked after a REST request from the
 * specified endpoint. The endpoint make reference to specific actions to carry out for solving the emergency plan
 * in BPMN format.
 */
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class NettunitController {

    NettunitService nettunitService;

    //********************************************************** deployment endpoints **********************************************************
    @PostMapping("/NETTUNIT/clearDeployments")
    public void clearDeployments() {
        nettunitService.clearAllExistingDeployments();
    }

    @PostMapping("/NETTUNIT/deploy")
    public void deployWorkflow() throws IOException {
        nettunitService.deployProcessDefinition();
    }

    @PostMapping("/NETTUNIT/deployProcess/{processID}")
    public void deployWorkflow(@PathVariable("processID") String processID, @RequestBody String processDef) {
        nettunitService.deployProcessDefinition(processID, processDef);
    }

    @GetMapping("/NETTUNIT/incident_list/{processDefinitionID}")
    public List<Map<String, Object>> getActiveProcessInstances(@PathVariable("processDefinitionID") String processDefinitionID) {
        List<Map<String, Object>> processInstanceMapping = new ArrayList<>();
        for (ProcessInstance pp : nettunitService.getActiveProcessInstances(processDefinitionID)) {
            Map<String, Object> map = new HashMap<>();//objectMapper.convertValue(lp, Map.class);
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
    public List<ProcessDefinition> getActiveProcesses() {
        return nettunitService.getActiveProcesses();
    }

    @GetMapping("/NETTUNIT/tasks/{processID}")
    public List<TaskDetails> getProcessDefinitions(@PathVariable("processID") String processID) {
        return nettunitService.getTasks(processID);
    }

    //********************************************************** process endpoints **********************************************************

    @PostMapping("/NETTUNIT/incident/apply")
    public void applyInterventionRequest(@RequestBody InterventionRequest interventionRequest) {
        nettunitService.applyInterventionRequest(interventionRequest);
    }

    @GetMapping("/equipe_interne/tasks/")
    public List<TaskDetails> getInternalEquipeTasks() {
        return nettunitService.getInternalEquipeTasks();
    }

    @GetMapping("/gestionnaire/tasks")
    public List<TaskDetails> getGestionnaireTasks() {
        return nettunitService.getGestionnaireTasks();
    }

    @GetMapping("/prefecture/tasks")
    public List<TaskDetails> getPrefectureTasks() {
        return nettunitService.getPrefectureTasks();
    }

    @GetMapping("/pompiers/tasks")
    public List<TaskDetails> getPompiersTasks() {
        return nettunitService.getPompiersTasks();
    }

    //********************************************************** GESTIONNAIRE **********************************************************

    @PostMapping("/gestionnaire/confirmer_notification/{taskID}")
    public void gestionnaire_confirmReceivedNotification(@RequestBody LoginToken loginToken, @PathVariable("taskID") String taskID) {
        nettunitService.gestionnaire_confirmReceivedNotification(loginToken, taskID);

    }

    @PostMapping("/gestionnaire/activer_plan_securite_interne/{taskID}")
    public void gestionnaire_activateInternalSecurityPlan(@RequestBody LoginToken loginToken, @PathVariable("taskID") String taskID) {
        nettunitService.gestionnaire_activateInternalSecurityPlan(taskID);
    }

    @PostMapping("/gestionnaire/selection_plan_du_repertoire/{taskID}")
    public void gestionnaire_selectPlanFromRepository(@PathVariable("taskID") String taskID) {
        nettunitService.gestionnaire_selectPlanFromRepository(taskID);
    }

    //****************************************************** DIRIGEANT PREFECTURE ******************************************************
    @PostMapping("/prefecture/confirmer_notification_evaluation/{taskID}")
    public void prefecture_receiveIncidentEvaluation(@PathVariable("taskID") String taskID) {
        nettunitService.prefecture_receiveIncidentEvaluation(taskID);
    }

    //****************************************************** POMPIERS ******************************************************
    @PostMapping("/pompiers/confirmer_notification_evaluation/{taskID}")
    public void firefighter_receiveReport(@PathVariable("taskID") String taskID) {
        nettunitService.firefighter_receiveReport(taskID);
    }

    @PostMapping("/pompiers/envoie_equipe_secours/{taskID}")
    public void firefighter_sendRescueTeam(@PathVariable("taskID") String taskID) {
        nettunitService.firefighter_sendRescueTeam(taskID);
    }

}
