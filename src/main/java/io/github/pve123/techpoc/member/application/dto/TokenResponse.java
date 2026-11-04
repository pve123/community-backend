package io.github.pve123.techpoc.member.application.dto;

public record TokenResponse(
        String grantType,
        String accessToken,
        String refreshToken
) {

    public static TokenResponse bearer(String accessToken, String refreshToken) {
        return new TokenResponse("Bearer", accessToken, refreshToken);
    }
}
