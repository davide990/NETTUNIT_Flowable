package nettunit.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProcessInstancesRegister {

    private static ProcessInstancesRegister instance;

    private List<ProcessInstanceResponse> processes;

    private ProcessInstancesRegister() {
        processes = new ArrayList<>();
    }

    public void add(ProcessInstanceResponse p) {
        processes.add(p);
    }

    public List<ProcessInstanceResponse> processes() {
        return processes;
    }

    public void setStatus(String processID, String status) {
        Optional<ProcessInstanceResponse> p = processes.stream().filter(pp -> pp.getProcessId().equals(processID)).findFirst();
        if (p.isPresent()) {
            processes.remove(p.get());
            ProcessInstanceResponse pNew = p.get().withStatus(status);
            processes.add(pNew);

        }
    }

    public static ProcessInstancesRegister get() {
        if (instance == null) {
            instance = new ProcessInstancesRegister();
        }
        return instance;
    }

}
