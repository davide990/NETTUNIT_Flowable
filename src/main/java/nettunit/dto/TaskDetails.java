package nettunit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class TaskDetails {
    String taskID;
    String taskName;
    String processID;
    /*String startTime;
    String endTime;
    String dueDate;*/
    Map<String, Object> taskData;
}
