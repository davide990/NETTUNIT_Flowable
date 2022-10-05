package nettunit.rabbitMQ.ConsumerService;

import RabbitMQ.JixelEvent;
import RabbitMQ.JixelEventUpdate;
import RabbitMQ.Recipient;
import nettunit.rabbitMQ.PendingMessageComponentListener;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

abstract public class Consumer {
    protected static int MAXIMUM_CONSUMER_MESSAGES_COUNT = 100000;

    public Optional<PendingMessageComponentListener> listener;

    /**
     * This map stores all the jixel events/updates/reports sent by MUSA from a specific task
     * until the message has been acknowledged from destination actor. Once acknowledged, the activity
     * of the process that provided the communication is completed
     */
    private Map<JixelEvent, String> pendingMessages;

    protected Thread consumerTask;

    protected Logger logger;

    public Consumer() {
        pendingMessages = new HashMap<>();
    }

    /**
     * When a message has been consumed by MUSA, related to the object given in input,
     * then the related task is completed.
     *
     * @param obj
     */
    public void completeTask(JixelEvent obj) {
        String pendingTaskID = getTaskID(obj);
        if (!pendingTaskID.isEmpty()) {
            remove(obj);
            listener.ifPresent(l -> l.completeTask(pendingTaskID));
            logger.info("Completed Task with ID: " + pendingTaskID);
        }
    }

    public void completeTaskByEvent(JixelEvent obj) {
        String pendingTaskID = getTaskID(obj);

        //Optional<JixelEvent> dd = pendingMessages.keySet().stream()
        //        .filter(ev ->ev.id() == obj.id()).findAny();

        boolean hasEvent = pendingMessages.keySet().stream().map(x->x.id()).collect(Collectors.toList()).contains(obj.id());
        if (hasEvent) {
            remove(obj);
            listener.ifPresent(l -> l.completeTask(pendingTaskID));
            logger.info("Completed Task with ID: " + pendingTaskID);
        }
    }


    public void setListener(PendingMessageComponentListener listener) {
        this.listener = Optional.of(listener);
    }


    public String getTaskID(JixelEvent obj) {
        return pendingMessages.getOrDefault(obj, "");
    }

    public boolean remove(JixelEvent obj) {
        String pendingTaskID = pendingMessages.getOrDefault(obj, "");
        if (!pendingTaskID.isEmpty()) {
            pendingMessages.remove(obj);
            return true;
        }
        return false;
    }

    /**
     * Save an object (a jixel event, a recipient, etc.) and associate it with a task ID.
     * When the object is sent as a message to MUSA, this is stored into a temporary data structure
     * until the message is consumed and the acknowledgment is sent back to jixel. When this acknowledgment
     * is received, we search for the object in the map, get the corresponding task, and complete it
     * to continue the process execution (in completeTask() method).
     *
     * @param obj    the domain entity to be temporary saved
     * @param taskID the task ID
     */
    public void save(JixelEvent obj, String taskID) {
        pendingMessages.put(obj, taskID);
    }

}
