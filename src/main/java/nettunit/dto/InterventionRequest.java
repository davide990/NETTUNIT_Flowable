package nettunit.dto;

import lombok.Data;

/**
 * An intervention request that can be submitted by user (the safety manager). Once received this
 * request by the system, a new emergency plan instance is created by flowable.
 */
@Data
public class InterventionRequest {
    String emergencyID;
    String empName;
    String requestDescription;
}