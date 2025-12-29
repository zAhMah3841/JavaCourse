package com.example.call_track.service;

import com.example.call_track.dto.user.RegistrationDto;
import com.example.call_track.entity.call.CallType;
import com.example.call_track.entity.user.User;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FakeDataService {
    private final UserService userService;
    private final CallService callService;

    @Value("${app.fake-data.max-users:10}")
    private int maxFakeUsers;

    @Value("${app.fake-data.credentials-file:fake_users.txt}")
    private String credentialsFilePath;

    private final Faker faker = new Faker(new Locale("en"));

    public void generateFakeData() throws IOException {
        List<FakeUserData> fakeUsers = createFakeUserData();

        Path usersFile = Path.of(credentialsFilePath);
        // Create file with header if it doesn't exist
        if (!Files.exists(usersFile)) {
            Files.writeString(usersFile, "Fake Users Credentials:\n\n", StandardOpenOption.CREATE);
        }

        for (FakeUserData data : fakeUsers) {
            try {
                User user = userService.registerUser(data.toRegistrationDto());
                appendToFile(usersFile, data.username + " : " + data.password + "\n");
                data.user = user;
            } catch (Exception e) {
                System.err.println("Failed to register user " + data.username + ": " + e.getMessage());
            }
        }

        // Generate calls between users
        generateFakeCalls(fakeUsers);
    }

    private List<FakeUserData> createFakeUserData() {
        List<FakeUserData> users = new java.util.ArrayList<>();
        java.util.Set<String> usedUsernames = new java.util.HashSet<>();
        java.util.Set<String> usedPhones = new java.util.HashSet<>();

        for (int i = 1; i <= maxFakeUsers; i++) {
            String username;
            do {
                username = generateUniqueUsername();
            } while (usedUsernames.contains(username));
            usedUsernames.add(username);

            String password = generateValidPassword(i);
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String middleName = faker.name().lastName(); // Using lastName as middle name for simplicity

            String phone;
            do {
                phone = generateValidPhoneNumber();
            } while (usedPhones.contains(phone));
            usedPhones.add(phone);

            users.add(new FakeUserData(username, password, firstName, lastName, middleName, phone));
        }
        return users;
    }

    private String generateUniqueUsername() {
        // Generate unique username using Faker, replace invalid characters with underscores
        return faker.internet().username().replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String generateValidPassword(int index) {
        // Generate a password that meets the requirements: 8-64 chars, uppercase, lowercase, digit, special char
        return "TempPass" + index + "!";
    }

    private String generateValidPhoneNumber() {
        // Generate Belarusian phone numbers in format +37529xxxxxxx or +37533xxxxxxx etc.
        String[] prefixes = {"29", "33", "44", "25"};
        String prefix = prefixes[faker.random().nextInt(prefixes.length)];
        String number = faker.number().digits(7);
        return "+375" + prefix + number;
    }

    private void generateFakeCalls(List<FakeUserData> users) {
        Random random = new Random();
        BigDecimal pricePerMinute = BigDecimal.valueOf(0.10); // 0.10 per minute

        // Filter only successfully registered users
        List<User> registeredUsers = users.stream()
                .filter(data -> data.user != null)
                .map(data -> data.user)
                .toList();

        for (int i = 0; i < registeredUsers.size(); i++) {
            for (int j = i + 1; j < registeredUsers.size(); j++) {
                User caller = registeredUsers.get(i);
                User callee = registeredUsers.get(j);

                // Random calls: some incoming, some outgoing
                CallType callType = random.nextBoolean() ? CallType.OUTGOING : CallType.INCOMING;
                long durationSeconds = 60 + random.nextInt(300); // 1-5 minutes

                try {
                    callService.createCall(caller, callee, callType, durationSeconds, pricePerMinute);
                } catch (Exception e) {
                    System.err.println("Failed to create call between " + caller.getUsername() + " and " + callee.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }

    private void appendToFile(Path filePath, String content) throws IOException {
        Files.writeString(filePath, content, StandardOpenOption.APPEND);
    }

    private static class FakeUserData {
        String username;
        String password;
        String firstName;
        String lastName;
        String middleName;
        String phone;
        User user;

        FakeUserData(String username, String password, String firstName, String lastName, String middleName, String phone) {
            this.username = username;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.middleName = middleName;
            this.phone = phone;
        }

        RegistrationDto toRegistrationDto() {
            return RegistrationDto.builder()
                    .username(username)
                    .password(password)
                    .firstName(firstName)
                    .lastName(lastName)
                    .middleName(middleName)
                    .phone(phone)
                    .build();
        }
    }
}