package com.apppang.apppang2.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    //Spring Boot가 application.yml 설정을 읽어서 자동으로 만들어주는 메일 발송 도구
    private final JavaMailSender javaMailSender;

    public void sendResetPAsswordEmail(String toEmail, String resetLink){
        //단순 텍스트 메일을 만들 수 있는 객체 생성
        SimpleMailMessage message = new SimpleMailMessage();

        //메일의 수신자, 제목, 본문
        message.setTo(toEmail);
        message.setSubject("[Apppang] 비밀번호 재설정 안내입니다.");
        message.setText("안녕하세요.\n" +
                "비밀번호 재설정을 위해 아래 링크를 클릭해주세요.\n\n" +
                resetLink + "\n\n" +
                "(이 링크는 10분 동안만 유효합니다. 본인이 요청하지 않은 경우 이 메일을 무시해 주세요.)");
        //메일 발송
        javaMailSender.send(message);
    }
}
