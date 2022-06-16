package nettunit.dto;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
public class ProcessInstanceResponse {
    String processId;
    @With
    public String status;
    boolean isEnded;
}
