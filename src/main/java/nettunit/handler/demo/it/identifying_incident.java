package nettunit.handler.demo.it;

import RabbitMQ.JixelEvent;
import nettunit.MUSA.StateOfWorldUpdateOp;
import nettunit.handler.base.BaseHandler;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class identifying_incident extends BaseHandler implements TriggerableActivityBehavior {

    private static Logger logger = LoggerFactory.getLogger(identifying_incident.class);

    String evolution_predicate = "identified_incident(volcano)";

    @Override
    public void trigger(DelegateExecution delegateExecution, String signalEvent, Object signalData) {
        this.getNETTUNITService().updateMUSAStateOfWorld(StateOfWorldUpdateOp.ADD, evolution_predicate, this.getClass().getName());
        logger.info("Capability executed correctly [" + delegateExecution.getId() + "]: " + this.getClass().getSimpleName());
    }

    @Override
    public void execute(DelegateExecution execution) {

        super.execute(execution);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());

        String taskName = ((ExecutionEntityImpl) execution).getActivityName();
        String taskID = ((ExecutionEntityImpl) execution).getActivityId();

        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);

        /*
        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.DIRECTOR_ETNEA_OBSERVATORY);
        this.getMUSAService().addRecipient(evt, recipients.toList());
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getMUSAService().updateCommType(evt, JixelDomainInformation.COMM_TYPE_PREOPERATIVA);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateEventTypology(evt, JixelDomainInformation.EVENT_TYPE_VULCANO);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateUrgencyLevel(evt, JixelDomainInformation.URGENCY_LEVEL_FUTURA);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateEventSeverity(evt, JixelDomainInformation.SEVERITY_LEVEL_MINORE);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateEventDescription(evt, "test");
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getNETTUNITService().currentTask = Optional.of(this.getClass().getName());
        this.getNETTUNITService().FailedTaskName = Optional.of(taskName);
        this.getNETTUNITService().FailedTaskImplementation = Optional.of(this.getClass().getName());
*/

        Thread t_comune = new Thread(() -> deployAndExecuteProcess("goalmodel_demo/process_comune.txt",
                "process_comune",
                evt));
        t_comune.start();

        Thread t_pcct = new Thread(() -> deployAndExecuteProcess("goalmodel_demo/process_pc_ct.txt",
                "process_pc_ct",
                evt));
        t_pcct.start();

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName() + " Waiting for ack...");
    }

    private void deployAndExecuteProcess(String goalModelFName, String processDefID, JixelEvent evt) {
        // 1. Read the goal model from file
        Optional<String> goalModelTN = Optional.empty();
        try {
            goalModelTN = Optional.ofNullable(readGoalModelFromFile(goalModelFName));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (goalModelTN.isPresent()) {
            //2. Invoke 'Goal2BPMN' in MUSA
            String MUSAAddress = getNETTUNITService().getEnvironment().getProperty("nettunit.musa.address");
            String MUSAPort = getNETTUNITService().getEnvironment().getProperty("nettunit.musa.port");
            String BPMNString = GoalModel2BPMN(goalModelTN.get(), processDefID, MUSAAddress, MUSAPort);

            //3. Deploy process in Flowable
            DeployToFlowable(BPMNString, processDefID, MUSAAddress, MUSAPort);

            //4. Create the instance of the new plan
            getNETTUNITService().applyInterventionRequest(evt, processDefID);
        }
    }

    private void DeployToFlowable(String BPMNString, String processDefID, String MUSAAddress, String MUSAPort) {
        /*String teeSymbol = "\u22A4";
        String repl = "${myVariable}";
        String newProcessDef = BPMNString.replace(teeSymbol, repl);
        getNETTUNITService().deployProcessDefinition(processDefID, newProcessDef);*/

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body = RequestBody.create(processDefID + ":" + BPMNString, mediaType);
        Request request = new Request.Builder()
                .url("http://" + MUSAAddress + ":" + MUSAPort + "/Deploy")
                .method("POST", body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            logger.info(response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String GoalModel2BPMN(String GoalModel, String planID, String MUSAAddress, String MUSAPort) {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");

        RequestBody body = RequestBody.create(planID + ":" + GoalModel, mediaType);
        Request request = new Request.Builder()
                .url("http://" + MUSAAddress + ":" + MUSAPort + "/Goal2BPMN")
                .method("POST", body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String readGoalModelFromFile(String goalModelPath) throws IOException, URISyntaxException {
        URL resource = this.getClass().getClassLoader().getResource(goalModelPath);
        if (resource == null) {
            logger.error("Unable to load COMUNE goal model");
            return null;
        } else {
            File goalModelFile = new File(resource.toURI());
            return FileUtils.readFileToString(goalModelFile, "utf-8");
        }
    }

}
