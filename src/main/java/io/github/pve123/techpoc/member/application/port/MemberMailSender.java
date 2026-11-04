package io.github.pve123.techpoc.member.application.port;

public interface MemberMailSender {

    void sendVerificationCode(String email, String code);

    void sendTemporaryPassword(String email, String temporaryPassword);
}
