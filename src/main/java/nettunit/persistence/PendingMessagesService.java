package nettunit.persistence;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class PendingMessagesService {

    /**
     * This map stores all the jixel events/updates/reports sent by MUSA from a specific task
     * until the message has been acknowledged from destination actor. Once acknowledged, the activity
     * of the process that provided the communication is completed
     */

    private Map<Object, String> pendingMessages;

    @Autowired
    public void PendingMessagesService() {
        pendingMessages = new HashMap<>();
    }

    public String getTaskID(Object obj) {
        return pendingMessages.getOrDefault(obj, "");
    }

    public boolean remove(Object obj) {
        String pendingTaskID = pendingMessages.getOrDefault(obj, "");
        if (!pendingTaskID.isEmpty()) {
            pendingMessages.remove(obj);
            return true;
        }
        return false;
    }

    public void save(Object obj, String taskID) {
        pendingMessages.put(obj, taskID);
    }

    @PostConstruct
    public void init() {
        System.out.println("hello from post construct");
    }
}
