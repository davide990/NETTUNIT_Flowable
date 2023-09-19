package nettunit.rabbitMQ.ConsumerService;

import RabbitMQ.JixelEvent;
import nettunit.NettunitService;
import nettunit.handler.notify_competent_body_internal_plan;
import nettunit.rabbitMQ.PendingMessageComponentListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

abstract public class Consumer {
    protected static int MAXIMUM_CONSUMER_MESSAGES_COUNT = 100000;

    public Optional<PendingMessageComponentListener> listener;

    /**
     * This map stores all the jixel events/updates/reports sent by MUSA from a specific task
     * until the message has been acknowledged from destination actor. Once acknowledged, the activity
     * of the process that provided the communication is completed
     */
    //private Map<JixelEvent, List<String>> pendingMessages;
    private Map<JixelEvent, List<String>> pendingMessages;

    private Map<JixelEvent, CountDownLatch> pendingServiceTaskMessages;

    protected Thread consumerTask;

    //protected Logger logger;
    protected Logger logger = LoggerFactory.getLogger(Consumer.class);

    public Consumer() {
        pendingMessages = new HashMap<>();
        pendingServiceTaskMessages = new HashMap<>();
    }

    public void applyInterventionRequest(JixelEvent evt) {
        listener.ifPresent(l -> l.applyInterventionRequest(evt));
    }

    public void completeTaskByEvent(int incidentID) {
        Optional<JixelEvent> obj = pendingMessages.keySet().stream().filter(ev -> ev.id() == incidentID).findFirst();
        boolean hasEvent = obj.isPresent();
        /*if (!hasEvent) {
            //maybe it's a service task?
            completeServiceTaskByEvent(obj.get());
            return;
        }*/
        Optional<String> taskToComplete = remove(obj.get());
        if (taskToComplete.isPresent()) {
            listener.ifPresent(l -> l.completeTask(obj.get(), taskToComplete.get()));
            logger.info("[JIXEL EVENT ID " + obj.get().id() + "] Completed Task with ID: " + taskToComplete.get());
            if (pendingMessages.containsKey(obj.get())) {
                logger.info("~~~~~~ Number of pending messages: " + pendingMessages.get(obj.get()).size());
            }

        }
    }

    public void completeTaskByEvent(JixelEvent obj) {
        /*boolean hasEvent = pendingMessages.keySet().stream().map(x -> x.id()).collect(Collectors.toList()).contains(obj.id());
        if (!hasEvent) {
            //maybe it's a service task?
            completeServiceTaskByEvent(obj);
            return;
        }*/


        //Optional<JixelEvent> theEvent = pendingMessages.keySet().stream().filter(ev -> ev.id() == obj.id()).findFirst();

        Optional<JixelEvent> theEvent = Optional.empty();
        if (obj.incident_id().isEmpty())
             theEvent = pendingMessages.keySet().stream().filter(ev -> ev.id() == obj.id()).findFirst();
        else
            theEvent = pendingMessages.keySet().stream().filter(ev -> ev.id() == (Integer) obj.incident_id().get()).findFirst();

        if (theEvent.isPresent()) {
            Optional<String> taskToComplete = remove(theEvent.get());
            //Optional<String> taskToComplete = remove(obj);
            if (taskToComplete.isPresent()) {
                listener.ifPresent(l -> l.completeTask(obj, taskToComplete.get()));
                logger.info("[JIXEL EVENT ID " + obj.id() + "] Completed Task with ID: " + taskToComplete.get());
            }
        } else {
            logger.warn("Unable to find JixelEvent with id [" + obj.id() + "] among pending messages queue.");
        }

    }

    public void completeServiceTaskByEvent(JixelEvent obj) {
        boolean hasEvent = pendingServiceTaskMessages.keySet().stream().map(x -> x.id())
                .collect(Collectors.toList()).contains(obj.id());
        if (!hasEvent) {
            return;
        }
        pendingServiceTaskMessages.get(obj).countDown();
    }


    public void setListener(PendingMessageComponentListener listener) {
        this.listener = Optional.of(listener);
    }


    public String getTaskID(JixelEvent evt) {

        if (pendingMessages.containsKey(evt)) {
            if (!pendingMessages.get(evt).isEmpty()) {
                logger.info("[Consumer] Completed Task(s) for event: " + pendingMessages.get(evt));
                return pendingMessages.get(evt).get(pendingMessages.get(evt).size() - 1);
            }
        }
        return "";
    }

    public Optional<String> remove(JixelEvent evt) {

        if (!pendingMessages.containsKey(evt)) {
            return Optional.empty();
        }
        if (pendingMessages.get(evt).isEmpty()) {
            return Optional.empty();
        }
        logger.info("[CONSUMER] Removing " + pendingMessages.get(evt).get(pendingMessages.get(evt).size() - 1));
        Optional<String> taskIDToComplete = Optional.of(pendingMessages.get(evt).get(pendingMessages.get(evt).size() - 1));
        pendingMessages.get(evt).remove(pendingMessages.get(evt).size() - 1);
        if (pendingMessages.get(evt).isEmpty()) {
            //the task can be completed
            logger.info("[CONSUMER] Empty queue for event " + evt.id() + "; last elem: " + taskIDToComplete.get());
            pendingMessages.remove(evt);
            return taskIDToComplete;
        }

        return Optional.empty();
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
        logger.info("[CONSUMER] Awaiting JIXEL ack for user task [" + taskID + "] for evt ID [" + evt.id() + "]");
        if (!pendingMessages.containsKey(evt)) {
            pendingMessages.put(evt, new ArrayList<>());
        }
        pendingMessages.get(evt).add(taskID);
    }

    public void saveServiceTask(JixelEvent evt, String taskID, CountDownLatch latch) {
        logger.info("[CONSUMER] Awaiting ack for service task [" + taskID + "] for evt ID [" + evt.id() + "]");
        pendingServiceTaskMessages.put(evt, latch);
    }

    public int getNumberOfPendingMessages(int jixelEventID) {
        Optional<JixelEvent> evt = pendingMessages.keySet().stream().filter(k -> k.id() == jixelEventID).findFirst();
        return evt.map(jixelEvent -> pendingMessages.get(jixelEvent).size()).orElse(0);
    }

}
