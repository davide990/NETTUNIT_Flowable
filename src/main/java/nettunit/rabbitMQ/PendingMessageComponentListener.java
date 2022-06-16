package nettunit.rabbitMQ;

public interface PendingMessageComponentListener {
    void completeTask(String taskID);
}
