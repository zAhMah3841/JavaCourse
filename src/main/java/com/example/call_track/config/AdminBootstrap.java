package com.example.call_track.config;


import com.example.call_track.entity.user.User;
import com.example.call_track.entity.user.UserRole;
import com.example.call_track.repository.UserRepository;
import com.example.call_track.service.AvatarService;
import com.example.call_track.service.FakeDataService;
import com.example.call_track.service.PhoneNumberService;
import jakarta.transaction.Transactional;
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
    private final FakeDataService fakeDataService;
    private final PhoneNumberService phoneNumberService;

    @Value("${app.admin.create-default:true}")
    private boolean createDefaultAdmin;

    @Value("${app.fake-data.create-on-startup:false}")
    private boolean createFakeDataOnStartup;

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
        if (createDefaultAdmin) {
            long adminCount = userRepository.countByRoleActive(UserRole.ADMIN);
            if (adminCount == 0 && !userRepository.existsByUsername(defaultUsername)) {
                createDefaultAdmin();
            }
        }

        if (createFakeDataOnStartup) {
            try {
                fakeDataService.generateFakeData();
                System.out.println("Fake data generated successfully");
            } catch (Exception e) {
                System.err.println("Failed to generate fake data: " + e.getMessage());
            }
        }
    }

    @Transactional
    private void createDefaultAdmin() {
        try {
            String avatarPath = avatarService.generateAvatar(defaultFirstName, defaultLastName);

            User admin = User.builder()
                    .username(defaultUsername)
                    .password(passwordEncoder.encode(defaultPassword))
                    .firstName(defaultFirstName)
                    .lastName(defaultLastName)
                    .role(UserRole.ADMIN)
                    .avatarPath(avatarPath)
                    .forcePasswordChange(true)
                    .enabled(true)
                    .accountNonExpired(true)
                    .accountNonLocked(true)
                    .credentialsNonExpired(true)
                    .build();

            admin = userRepository.save(admin);
            phoneNumberService.addPhoneNumber(admin, defaultPhone, true);
            System.out.println("Default admin created with username: " + defaultUsername + " and phone: " + defaultPhone);
        } catch (Exception e) {
            System.err.println("Failed to create default admin: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to rollback transaction
        }
    }
}