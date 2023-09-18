package nettunit.handler.demo.it;

import RabbitMQ.JixelEvent;
import nettunit.JixelDomainInformation;
import nettunit.MUSA.StateOfWorldUpdateOp;
import nettunit.handler.base.BaseHandler;
import nettunit.rabbitMQ.ProducerService.MUSAProducerService;
import okhttp3.*;
import org.apache.commons.io.FileUtils;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.delegate.TriggerableActivityBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.collection.mutable.ArrayBuffer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import static nettunit.NettunitService.JIXEL_EVENT_VAR_NAME;

public class inform_pcct extends BaseHandler implements TriggerableActivityBehavior {

    private static Logger logger = LoggerFactory.getLogger(inform_pcct.class);

    String evolution_predicate = "involved_competent_roles(pcct)";

    @Override
    public void trigger(DelegateExecution delegateExecution, String signalEvent, Object signalData) {
        this.getNETTUNITService().updateMUSAStateOfWorld(StateOfWorldUpdateOp.ADD, evolution_predicate, this.getClass().getName());
        logger.info("Capability executed correctly [" + delegateExecution.getId() + "]: " + this.getClass().getSimpleName());
    }

    @Override
    public void execute(DelegateExecution execution) {

        super.execute(execution);

        logger.info("Executing capability [" + execution.getId() + "]: " + this.getClass().getSimpleName());
        getNETTUNITService().currentTask = Optional.of(this.getClass().getName());

        MUSAProducerService musaService = getMUSAService();
        JixelEvent evt = (JixelEvent) execution.getVariable(JIXEL_EVENT_VAR_NAME);

        String taskName = ((ExecutionEntityImpl) execution).getActivityName();
        String taskID = ((ExecutionEntityImpl) execution).getActivityId();

        ArrayBuffer recipients = new ArrayBuffer<>();
        recipients.addOne(JixelDomainInformation.PCCT);
        musaService.addRecipient(evt, recipients.toList());
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        musaService.updateCommType(evt, JixelDomainInformation.COMM_TYPE_OPERATIVA);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        musaService.updateEventSeverity(evt, JixelDomainInformation.SEVERITY_LEVEL_ELEVATO);
        this.getMusaRabbitMQConsumerService().save(evt, taskID);
        this.getMUSAService().updateEventDescription(evt, "test *presa in carico dal DRPC* [PCRS] + *interpretazione dei risultati del modello* [INM] + *valutazione del potenziale impatto sulla salute della popolazione interessata* [CNR-IFT] + *comunicazione attivazione dello stato di ALLARME, l&#39;attivazione del COC e del modello di intervento* [Prefetto]");
        this.getMusaRabbitMQConsumerService().save(evt, taskID);

        this.getNETTUNITService().currentTask = Optional.of(this.getClass().getName());
        this.getNETTUNITService().FailedTaskName = Optional.of(taskName);
        this.getNETTUNITService().FailedTaskImplementation = Optional.of(this.getClass().getName());

        // Launch MUSA->NewGoal(process_pc_ct)
        Thread t_pcct = new Thread(() -> deployAndExecuteProcess("goalmodel_demo/process_pc_ct.txt",
                "process_pc_ct",
                evt));
        t_pcct.start();
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
