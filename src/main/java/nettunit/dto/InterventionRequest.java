package nettunit.dto;

import lombok.Data;

@Data
public class InterventionRequest {
    String emergencyID;
    String empName;
    String requestDescription;
}