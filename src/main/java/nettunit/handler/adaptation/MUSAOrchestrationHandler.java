package nettunit.handler.adaptation;

import nettunit.NettunitService;
import nettunit.SpringContext;
import nettunit.dto.TaskDetails;
import okhttp3.*;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;

public class MUSAOrchestrationHandler implements JavaDelegate {
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(MUSAOrchestrationHandler.class);

    @Override
    public void execute(DelegateExecution execution) {
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);

        //String failedTask = nettunit.FailedTaskImplementation.get();
        String failedTask = nettunit.FailedTaskImplementation.get();


        String MUSAAddress = nettunit.getEnvironment().getProperty("nettunit.musa.address");
        String MUSAPort = nettunit.getEnvironment().getProperty("nettunit.musa.port");

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        // The body of the POST request contains the full name of the failing service
        RequestBody body = RequestBody.create(failedTask, mediaType);
        Request request = new Request.Builder()
                .url("http://" + MUSAAddress + ":" + MUSAPort + "/FailCapability")
                .method("POST", body)
                .build();
        try {
            Response response = client.newCall(request).execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String taskID = execution.getId();
        String processID = execution.getProcessInstanceId();
        String taskName = ((ExecutionEntityImpl) execution).getActivityName();

        // Tell flowable that the adaptation task has finished its execution
        if (!nettunit.completedServiceTasksByEvents.containsKey(processID)) {
            nettunit.completedServiceTasksByEvents.put(processID, new ArrayList<>());
        }
        nettunit.completedServiceTasksByEvents.get(processID).add(new TaskDetails(taskID,
                taskName,
                processID,
                new HashMap<>()));

        logger.info("[" + DATE_FORMATTER.format(new Date()) + "-ADAPTATION TASK] Sent adaptivity request to MUSA.");
    }
}
