package org.example.sbb.user;

import lombok.RequiredArgsConstructor;
import org.example.sbb.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    public SiteUser create(String username, String email, String password, String nickname) {
        SiteUser siteUser = new SiteUser();
        siteUser.setUsername(username);
        siteUser.setEmail(email);
        siteUser.setPassword(passwordEncoder.encode(password));
        siteUser.setNickname(nickname);
        siteUser.setRole(Role.USER);
        this.userRepository.save(siteUser);
        return siteUser;
    }

    public SiteUser getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    public SiteUser getUserFromEmail(String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) return siteUser.get();
        else throw new DataNotFoundException("site user not found");
    }

    public String sendTempPasswordEmail(String email) {
        StringBuilder password = new StringBuilder(10);
        if (userRepository.findByEmail(email).isPresent()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tigerrla24@gmail.com");
            message.setTo(email);
            message.setSubject("임시 비밀번호");

            // 비밀번호 생성 로직 (10글자)
            for (int i = 0; i < 10; i++) {
                int index = RANDOM.nextInt(CHARACTERS.length());
                password.append(CHARACTERS.charAt(index));
            }
            message.setText(password.toString());
            mailSender.send(message);
        }
        return password.toString();
    }

    public void modifyPassword(SiteUser siteUser, String username, String email, String password) {
        siteUser.setUsername(username);
        siteUser.setEmail(email);
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
    }

    public boolean checkPassword(String currentPW, String inputPW) {
        if(passwordEncoder.matches(inputPW, currentPW)) return true;
        else return false;
    }

    public void modifyInfo(SiteUser siteUser, String name, String email, String password) {
        siteUser.setUsername(name);
        siteUser.setEmail(email);
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
    }

    public SiteUser getUserByKakaoId(String kakaoId) {
        return userRepository.findByKakaoId(kakaoId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with kakaoId: " + kakaoId));
    }
}
