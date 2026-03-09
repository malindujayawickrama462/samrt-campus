package com.smartcampus.security;

import com.smartcampus.entity.User;
import com.smartcampus.enums.Role;
import com.smartcampus.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");
        String sub = oAuth2User.getAttribute("sub");

        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(email)
                        .name(name)
                        .pictureUrl(picture)
                        .provider("google")
                        .providerId(sub)
                        .role(Role.USER)
                        .active(true)
                        .build())
        );

        // Update picture/name if changed
        user.setName(name);
        user.setPictureUrl(picture);
        userRepository.save(user);

        String token = tokenProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        String redirectUrl = frontendUrl.split(",")[0] + "/oauth2/redirect?token=" + token;

        log.info("OAuth2 login success for user: {}", email);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
