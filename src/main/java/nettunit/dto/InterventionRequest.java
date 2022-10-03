package nettunit.dto;

import lombok.Data;

/**
 * Once received this request (in form of JSon after a POST request), a new emergency plan instance is created by flowable.
 */
@Data
public class InterventionRequest {
    String emergencyPlanID;
    String empName;
    String requestDescription;
}