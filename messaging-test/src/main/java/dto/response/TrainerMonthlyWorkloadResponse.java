package dto.response;

public record TrainerMonthlyWorkloadResponse(
        String username,
        String firstName,
        String lastName,
        Boolean isActive,
        Integer year,
        Integer month,
        Integer trainingDurationTotal
){
}
