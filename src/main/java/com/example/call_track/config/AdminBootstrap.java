package com.example.call_track.config;


import com.example.call_track.entity.user.User;
import com.example.call_track.entity.user.UserRole;
import com.example.call_track.repository.UserRepository;
import com.example.call_track.service.AvatarService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AvatarService avatarService;

    @Value("${app.admin.create-default:true}")
    private boolean createDefaultAdmin;

    @Value("${app.admin.default-username:admin}")
    private String defaultUsername;

    @Value("${app.admin.default-password:changeMe123!}")
    private String defaultPassword;

    @Value("${app.admin.default-first-name:System}")
    private String defaultFirstName;

    @Value("${app.admin.default-last-name:Administrator}")
    private String defaultLastName;

    @Value("${app.admin.default-phone:+1234567890}")
    private String defaultPhone;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!createDefaultAdmin) {
            return;
        }

        long adminCount = userRepository.countByRole(UserRole.ADMIN);
        if (adminCount == 0) {
            createDefaultAdmin();
        }
    }

    private void createDefaultAdmin() {
        String avatarPath = avatarService.generateAvatar(defaultFirstName, defaultLastName);

        User admin = User.builder()
                .username(defaultUsername)
                .password(passwordEncoder.encode(defaultPassword))
                .firstName(defaultFirstName)
                .lastName(defaultLastName)
                .role(UserRole.ADMIN)
                .avatarPath(avatarPath)
                .forcePasswordChange(true) // Will add this field next
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        userRepository.save(admin);
        System.out.println("Default admin created with username: " + defaultUsername);
    }
}