package io.github.pve123.techpoc.member.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 소셜 로그인(구글·카카오·네이버) 데모 화면입니다.
 * 버튼을 누르면 스프링 시큐리티가 /oauth2/authorization/{provider}로 로그인 과정을 처리합니다.
 */
@Controller
public class SocialLoginPageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/sns")
    public String loginPage() {
        return "sns";
    }

    @GetMapping("/login-success")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User user, Model model) {
        model.addAttribute("name", user.getName());
        return "login-success";
    }

    @GetMapping("/login-failure")
    public String loginFailure() {
        return "login-failure";
    }
}
