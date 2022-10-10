package nettunit;

import RabbitMQ.Config;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import nettunit.dto.InterventionRequest;
import nettunit.dto.TaskDetails;
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

    @GetMapping("/NETTUNIT/active_incident_list/{processDefinitionID}")
    public List<Map<String, Object>> getActiveProcessInstances(@PathVariable("processDefinitionID") String processDefinitionID) {
        List<Map<String, Object>> processInstanceMapping = new ArrayList<>();
        for (ProcessInstance pp : nettunitService.getActiveProcessInstances(processDefinitionID)) {
            Map<String, Object> map = new HashMap<>();//objectMapper.convertValue(lp, Map.class);
            //pp.getProcessVariables()

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

    //********************************************************** process endpoints **********************************************************

    @PostMapping("/NETTUNIT/incident/apply")
    public void applyInterventionRequest(@RequestBody InterventionRequest interventionRequest) {
        nettunitService.applyInterventionRequest(interventionRequest);
    }

    @GetMapping("/NETTUNIT/task_list/{processID}")
    public List<TaskDetails> getAllTasks(@PathVariable("processID") String processID) {
        return nettunitService.getTasks(processID);
    }

    @PostMapping("/NETTUNIT/safety_manager/send_team_to_evaluate/{taskID}")
    public void send_team_to_evaluate(@PathVariable("taskID") String taskID) {
        nettunitService.send_team_to_evaluate(taskID);
    }

    @PostMapping("/NETTUNIT/plant_operator/activate_internal_security_plan/{taskID}")
    public void activate_internal_security_plan(@PathVariable("taskID") String taskID) {
        nettunitService.activate_internal_security_plan(taskID);
    }

    @PostMapping("/NETTUNIT/commander_fire_brigade/decide_response_type/{taskID}")
    public void decide_response_type(@PathVariable("taskID") String taskID) {
        nettunitService.decide_response_type(taskID);
    }

    @PostMapping("/NETTUNIT/prefect/declare_pre_alert_state/{taskID}")
    public void declare_pre_alert_state(@PathVariable("taskID") String taskID) {
        nettunitService.declare_pre_alert_state(taskID);
    }

    @PostMapping("/NETTUNIT/ARPA/evaluate_fire_radiant_energy/{taskID}")
    public void evaluate_fire_radiant_energy(@PathVariable("taskID") String taskID) {
        nettunitService.evaluate_fire_radiant_energy(taskID);
    }

    @PostMapping("/NETTUNIT/prefect/declare_alarm_state/{taskID}")
    public void declare_alarm_state(@PathVariable("taskID") String taskID) {
        nettunitService.declare_alarm_state(taskID);
    }


    /**/

    @PostMapping("/NETTUNIT/fail/{taskName}")
    public void fail_task(@PathVariable("taskName") String taskName) { nettunitService.failTask(taskName); }





}
