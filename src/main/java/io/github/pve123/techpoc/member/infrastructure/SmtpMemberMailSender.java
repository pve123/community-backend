package io.github.pve123.techpoc.member.infrastructure;

import io.github.pve123.techpoc.member.application.port.MemberMailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class SmtpMemberMailSender implements MemberMailSender {

    private final JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String from;

    @Override
    public void sendVerificationCode(String email, String code) {
        send(email, "[backend-tech-poc] 인증 코드",
                "인증 코드는 %s 입니다. 5분 안에 입력해 주세요.".formatted(code));
    }

    @Override
    public void sendTemporaryPassword(String email, String temporaryPassword) {
        send(email, "[backend-tech-poc] 임시 비밀번호",
                "임시 비밀번호는 %s 입니다. 로그인한 뒤 꼭 비밀번호를 바꿔 주세요.".formatted(temporaryPassword));
    }

    private void send(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }
}
