package nettunit.persistence;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class PendingMessage implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String taskID;

    private String commType;

    protected PendingMessage() {
    }

    public PendingMessage(String taskID, String commType) {
        this.taskID = taskID;
        this.commType = commType;
    }

    @Override
    public String toString() {
        return "PendingCommMessage{" +
                "id=" + id +
                ", taskID='" + taskID + '\'' +
                ", commType='" + commType + '\'' +
                '}';
    }

    public Long getId() {
        return id;
    }

    public String getTaskID() {
        return taskID;
    }

    public String getCommType() {
        return commType;
    }
}
