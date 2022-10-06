package nettunit.rabbitMQ.ConsumerService;

import RabbitMQ.JixelEvent;
import nettunit.handler.notify_competent_body_internal_plan;
import nettunit.rabbitMQ.PendingMessageComponentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

abstract public class Consumer {
    protected static int MAXIMUM_CONSUMER_MESSAGES_COUNT = 100000;

    public Optional<PendingMessageComponentListener> listener;

    /**
     * This map stores all the jixel events/updates/reports sent by MUSA from a specific task
     * until the message has been acknowledged from destination actor. Once acknowledged, the activity
     * of the process that provided the communication is completed
     */
    private Map<JixelEvent, List<String>> pendingMessages;

    protected Thread consumerTask;

    //protected Logger logger;
    protected Logger logger = LoggerFactory.getLogger(Consumer.class);

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
            listener.ifPresent(l -> l.completeTask(obj, pendingTaskID));
            logger.info("[JIXEL EVENT ID " + obj.id() + "] Completed Task with ID: " + pendingTaskID);
        }
    }

    public void completeTaskByEvent(JixelEvent obj) {
        String pendingTaskID = getTaskID(obj);

        boolean hasEvent = pendingMessages.keySet().stream().map(x -> x.id()).collect(Collectors.toList()).contains(obj.id());
        if (hasEvent) {
            if (remove(obj)) {
                listener.ifPresent(l -> l.completeTask(obj, pendingTaskID));
                logger.info("[JIXEL EVENT ID " + obj.id() + "] Completed Task with ID: " + pendingTaskID);
            }
        }
    }


    public void setListener(PendingMessageComponentListener listener) {
        this.listener = Optional.of(listener);
    }


    public String getTaskID(JixelEvent evt) {
        if (pendingMessages.containsKey(evt)) {
            if (!pendingMessages.get(evt).isEmpty()) {
                return pendingMessages.get(evt).get(pendingMessages.get(evt).size() - 1);
            }
        }
        return "";
    }

    public boolean remove(JixelEvent evt) {
        if (!pendingMessages.containsKey(evt)) {
            return false;
        }
        if (!pendingMessages.get(evt).isEmpty()) {
            //remove the last
            pendingMessages.get(evt).remove(pendingMessages.get(evt).size() - 1);
        }
        //check for size
        if (pendingMessages.get(evt).isEmpty()) {
            pendingMessages.remove(evt);
            return true; //no more ack, the task can be completed
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
     * @param evt    the domain entity to be temporary saved
     * @param taskID the task ID
     */
    public void save(JixelEvent evt, String taskID) {
        if (!pendingMessages.containsKey(evt)) {
            pendingMessages.put(evt, new ArrayList<>());
        }
        pendingMessages.get(evt).add(taskID);
    }

}
