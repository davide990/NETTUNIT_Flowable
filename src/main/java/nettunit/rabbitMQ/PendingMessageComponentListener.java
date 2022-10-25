package nettunit.rabbitMQ;

import RabbitMQ.JixelEvent;

public interface PendingMessageComponentListener {
    void completeTask(JixelEvent evt, String taskID);

    void applyInterventionRequest(JixelEvent evt);
}
