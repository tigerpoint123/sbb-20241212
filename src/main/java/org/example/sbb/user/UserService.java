package org.example.sbb.user;

import lombok.RequiredArgsConstructor;
import org.example.sbb.DataNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private JavaMailSender mailSender;

    public SiteUser create(String username, String email, String password) {
        SiteUser siteUser = new SiteUser();
        siteUser.setUsername(username);
        siteUser.setEmail(email);
        siteUser.setPassword(passwordEncoder.encode(password));
        this.userRepository.save(siteUser);
        return siteUser;
    }

    public SiteUser getUser(String username) {
        Optional<SiteUser> siteUser = this.userRepository.findByUsername(username);
        if (siteUser.isPresent()) return siteUser.get();
        else throw new DataNotFoundException("site user not found");
    }

    public SiteUser getUserFromEmail(String email) {
        Optional<SiteUser> siteUser = this.userRepository.findByEmail(email);
        if (siteUser.isPresent()) return siteUser.get();
        else throw new DataNotFoundException("site user not found");
    }

    public String sendTempPasswordEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tigerrla24@gmail.com");
            message.setTo(email);
            message.setSubject("임시 비밀번호");
            message.setText("123456");
            mailSender.send(message);
        }
        return "123456";
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
}
