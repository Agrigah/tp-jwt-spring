package ma.enset.tpjwtspring.dto;

public record RegisterRequest(
        String email,
        String password
) {
}
