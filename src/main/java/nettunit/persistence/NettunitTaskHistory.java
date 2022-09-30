package nettunit.persistence;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.flowable.engine.HistoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class NettunitTaskHistory {

    //flowable history service. This is automatically injected
    HistoryService historyService;

    /**
     * Get the list of unfinished tasks
     *
     * @return a list of task ID
     */
    public List<String> getUnfinishedProcessIDs() {
        return historyService.createHistoricProcessInstanceQuery().unfinished().list()
                .stream().map(h -> h.getId()).collect(Collectors.toList());
    }

    /**
     * Check if the input task ID is unfinished
     *
     * @param taskID
     */
    public void isUnfinishedTask(String taskID) {
        historyService.createHistoricTaskInstanceQuery().list()
                .stream().filter(p -> p.getId().equals(taskID)).findAny().isPresent();
    }

    /**
     * Return the list of active process instances
     *
     * @param processDefinitionKey
     * @return
     */
    public List<HistoricProcessInstance> activeProcesses(String processDefinitionKey) {
        return historyService.createHistoricProcessInstanceQuery()
                .unfinished()
                .processDefinitionKey(processDefinitionKey)
                .list();
    }


    /**
     * Get 10 HistoricProcessInstances that are finished and which took the most time to complete
     * (the longest duration) of all finished processes with the given definition ID.
     *
     * @return
     */
    public List<HistoricProcessInstance> longestEmergencyResponses(String processDefinitionKey) {
        return historyService.createHistoricProcessInstanceQuery()
                .finished()
                .processDefinitionKey(processDefinitionKey)
                .orderByProcessInstanceDuration().desc()
                .listPage(0, 10);
    }

    /**
     * Return the number of seconds elapsed since the task (which ID is the parameter) started its execution
     *
     * @param taskID
     * @return number of seconds elapsed, -1 if the task is not available
     */
    public long getTimeElapsed(String taskID) {

        Optional<HistoricTaskInstance> inst = historyService.createHistoricTaskInstanceQuery().list().stream()
                .filter(x -> x.getId().equals(taskID)).findFirst();

        if (inst.isPresent()) {
            Date d1 = inst.get().getCreateTime();
            LocalDateTime date = LocalDateTime.ofInstant(d1.toInstant(), ZoneId.systemDefault());
            LocalDateTime now = LocalDateTime.now();
            return ChronoUnit.SECONDS.between(date, now);
        }
        return -1;
    }

    public List<HistoricTaskInstance> getFinishedTasks(String processID) {
        return historyService.createHistoricTaskInstanceQuery().finished().list().stream()
                .filter(x -> x.getProcessInstanceId().equals(processID)).collect(Collectors.toList());
    }

    public List<HistoricTaskInstance> getUnfinishedTasks(String processID) {
        return historyService.createHistoricTaskInstanceQuery().list().stream()
                .filter(x -> x.getProcessInstanceId().equals(processID) &&
                        !Optional.ofNullable(x.getEndTime()).isPresent()).collect(Collectors.toList());
    }

    public List<HistoricTaskInstance> getTasksCompletedOn(Date date, String processID) {
        return historyService.createHistoricTaskInstanceQuery().taskCompletedOn(date).list().stream()
                .filter(x -> x.getProcessInstanceId().equals(processID)).collect(Collectors.toList());
    }

    public List<HistoricTaskInstance> getTasksCompletedAfter(Date date, String processID) {
        return historyService.createHistoricTaskInstanceQuery().taskCompletedAfter(date).list().stream()
                .filter(x -> x.getProcessInstanceId().equals(processID)).collect(Collectors.toList());
    }

    public List<HistoricTaskInstance> getTasksCompletedBefore(Date date, String processID) {
        return historyService.createHistoricTaskInstanceQuery().taskCompletedBefore(date).list().stream()
                .filter(x -> x.getProcessInstanceId().equals(processID)).collect(Collectors.toList());
    }

    /**
     * Measure the time elapsed between the create time of both input tasks
     *
     * @param taskID1
     * @param taskID2
     * @return number of seconds elapsed, -1 if any of the two tasks are not available
     */
    public long getStartToStartElapsedTime(String taskID1, String taskID2) {
        //Please note that I use findAny, however the ID of the task is unique
        Optional<HistoricTaskInstance> task1 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID1)).findAny();

        Optional<HistoricTaskInstance> task2 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID2)).findAny();

        if (!task1.isPresent() || !task2.isPresent()) {
            return -1;
        }

        LocalDateTime date1 = LocalDateTime.ofInstant(task1.get().getCreateTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime date2 = LocalDateTime.ofInstant(task2.get().getCreateTime().toInstant(), ZoneId.systemDefault());
        return ChronoUnit.SECONDS.between(date1, date2);
    }

    /**
     * Measure the time elapsed between the create time of first task and the end date (if any) of second task.
     *
     * @param taskID1
     * @param taskID2
     * @return number of seconds elapsed, -1 if any of the two tasks are not available
     */
    public long getStartToEndElapsedTime(String taskID1, String taskID2) {
        //Please note that I use findAny, however the ID of the task is unique
        Optional<HistoricTaskInstance> task1 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID1)).findAny();

        Optional<HistoricTaskInstance> task2 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID2)).findAny();

        if (!task1.isPresent() || !task2.isPresent()) {
            return -1;
        }
        if (Optional.ofNullable(task2.get().getEndTime()).isEmpty()) {
            return -1;
        }

        LocalDateTime date1 = LocalDateTime.ofInstant(task1.get().getCreateTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime date2 = LocalDateTime.ofInstant(task2.get().getEndTime().toInstant(), ZoneId.systemDefault());
        return ChronoUnit.SECONDS.between(date1, date2);
    }

    /**
     * Measure the time elapsed between the end time of first task (if any) and the create date of second task.
     *
     * @param taskID1
     * @param taskID2
     * @return number of seconds elapsed, -1 if any of the two tasks are not available
     */
    public long getEndToStartElapsedTime(String taskID1, String taskID2) {
        //Please note that I use findAny, however the ID of the task is unique
        Optional<HistoricTaskInstance> task1 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID1)).findAny();

        Optional<HistoricTaskInstance> task2 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID2)).findAny();

        if (!task1.isPresent() || !task2.isPresent()) {
            return -1;
        }
        if (Optional.ofNullable(task1.get().getEndTime()).isEmpty()) {
            return -1;
        }

        LocalDateTime date1 = LocalDateTime.ofInstant(task1.get().getEndTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime date2 = LocalDateTime.ofInstant(task2.get().getCreateTime().toInstant(), ZoneId.systemDefault());
        return ChronoUnit.SECONDS.between(date1, date2);
    }

    /**
     * Measure the time elapsed between the end time (if any) of the first and the second task.
     *
     * @param taskID1
     * @param taskID2
     * @return number of seconds elapsed, -1 if any of the two tasks are not available
     */
    public long getEndToEndElapsedTime(String taskID1, String taskID2) {
        //Please note that I use findAny, however the ID of the task is unique
        Optional<HistoricTaskInstance> task1 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID1)).findAny();

        Optional<HistoricTaskInstance> task2 = historyService.createHistoricTaskInstanceQuery().
                list().stream().filter(x -> x.getId().equals(taskID2)).findAny();

        if (!task1.isPresent() || !task2.isPresent()) {
            return -1;
        }
        if (Optional.ofNullable(task1.get().getEndTime()).isEmpty() ||
                Optional.ofNullable(task1.get().getEndTime()).isEmpty()) {
            return -1;
        }

        LocalDateTime date1 = LocalDateTime.ofInstant(task1.get().getEndTime().toInstant(), ZoneId.systemDefault());
        LocalDateTime date2 = LocalDateTime.ofInstant(task2.get().getEndTime().toInstant(), ZoneId.systemDefault());
        return ChronoUnit.SECONDS.between(date1, date2);
    }


}
