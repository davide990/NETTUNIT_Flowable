package nettunit.handler.adaptation;

import nettunit.NettunitService;
import nettunit.SpringContext;
import okhttp3.*;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MUSAOrchestrationHandler implements JavaDelegate {
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static Logger logger = LoggerFactory.getLogger(MUSAOrchestrationHandler.class);

    @Override
    public void execute(DelegateExecution execution) {
        NettunitService nettunit = SpringContext.getBean(NettunitService.class);
        String failedTask = nettunit.FailingTaskName.get();
        String MUSAAddress = nettunit.getEnvironment().getProperty("nettunit.musa.address");
        String MUSAPort = nettunit.getEnvironment().getProperty("nettunit.musa.port");

        OkHttpClient client = new OkHttpClient().newBuilder().build();
        MediaType mediaType = MediaType.parse("text/plain");
        // The body of the POST request contains the name of the service which failed its execution
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
        logger.info("[" + DATE_FORMATTER.format(new Date()) + "] Sent adaptivity request to MUSA.");
    }
}
