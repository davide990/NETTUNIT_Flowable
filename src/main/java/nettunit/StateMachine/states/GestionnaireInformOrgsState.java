package nettunit.StateMachine.states;

import nettunit.StateMachine.CurrentProcessContext;
import nettunit.StateMachine.ProcessState;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;

public class GestionnaireInformOrgsState implements ProcessState {

    public static String NETTUNIT_ADDRESS = "";

    @Override
    public void act() {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(NETTUNIT_ADDRESS);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");

        try {
            CloseableHttpResponse response = client.execute(httpPost); // POST request
            int statusCode = response.getStatusLine().getStatusCode();
            assert statusCode == 200 : "Expected 200, received " + Integer.toString(statusCode);
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void next(CurrentProcessContext pkg) {
        pkg.setState(new DetermineScenarioState());
    }

    @Override
    public void prev(CurrentProcessContext pkg) {
    }
}
