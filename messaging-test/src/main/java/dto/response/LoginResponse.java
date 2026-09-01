package dto.response;

public record LoginResponse(
        String token,
        String type
) {
}
