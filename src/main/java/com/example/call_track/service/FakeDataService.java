package com.example.call_track.service;

import com.example.call_track.dto.user.RegistrationDto;
import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.call.CallType;
import com.example.call_track.entity.user.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class FakeDataService {
    private final UserService userService;
    private final CallService callService;
    private final PhoneNumberService phoneNumberService;

    @Value("${app.fake-data.max-users:10}")
    private int maxFakeUsers;

    @Value("${app.fake-data.credentials-file:fake_users.txt}")
    private String credentialsFilePath;

    private final Faker fakerRu = new Faker(new Locale("ru"));
    private final Faker fakerEn = new Faker(new Locale("en"));
    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

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
                if (data.publicContactInfo != null) {
                    user.setPublicContactInfo(data.publicContactInfo);
                    userService.save(user);
                }
                appendToFile(usersFile, data.username + " : " + data.password + "\n");
                data.user = user;
            } catch (Exception e) {
                System.err.println("Failed to register user " + data.username + ": " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("Cause: " + e.getCause().getMessage());
                }
                // Continue with other users even if one fails
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
            String firstName = fakerRu.name().firstName();
            String lastName = fakerRu.name().lastName();
            String middleName = fakerRu.name().lastName(); // Using lastName as middle name for simplicity

            String phone;
            do {
                phone = generateValidPhoneNumber();
            } while (usedPhones.contains(phone));
            usedPhones.add(phone);

            String publicContactInfo = null;
            if (fakerEn.random().nextInt(10) < 3) { // 30% chance
                String email = fakerEn.internet().emailAddress();
                publicContactInfo = "Можно написать на email: " + email;
            }

            users.add(new FakeUserData(username, password, firstName, lastName, middleName, phone, publicContactInfo));
        }
        return users;
    }

    private String generateUniqueUsername() {
        // Generate unique username using Faker, replace invalid characters with underscores
        return fakerEn.internet().username().replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String generateValidPassword(int index) {
        // Generate a password that meets the requirements: 8-64 chars, uppercase, lowercase, digit, special char
        return "TempPass" + index + "!";
    }

    private String generateValidPhoneNumber() {
        // Generate valid Belarusian phone numbers
        // Belarus country code: +375, mobile operators: 29, 33, 44, 25
        String[] mobileOperators = {"29", "33", "44", "25"};
        String operator = mobileOperators[fakerEn.random().nextInt(mobileOperators.length)];

        // Try to generate a valid phone number (max 20 attempts)
        for (int attempt = 0; attempt < 20; attempt++) {
            try {
                // Generate 7-digit subscriber number (1000000 to 9999999)
                int subscriber = fakerEn.random().nextInt(1000000, 9999999);
                String fullNumber = "+375" + operator + String.format("%07d", subscriber);

                // Parse and validate using libphonenumber
                Phonenumber.PhoneNumber phoneNumber = phoneNumberUtil.parse(fullNumber, null);

                // Check if it's a valid number and is of a callable type
                if (phoneNumberUtil.isValidNumber(phoneNumber)) {
                    PhoneNumberUtil.PhoneNumberType type = phoneNumberUtil.getNumberType(phoneNumber);
                    // Check if it's a mobile, fixed line, or VoIP number
                    if (type == PhoneNumberUtil.PhoneNumberType.MOBILE ||
                            type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE ||
                            type == PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE ||
                            type == PhoneNumberUtil.PhoneNumberType.VOIP) {
                        return fullNumber;
                    }
                }
            } catch (Exception e) {
                // Continue to next attempt
            }
        }

        // Fallback: use a simple format that should work
        // Generate a number in format +375[operator][7 digits]
        int subscriber = fakerEn.random().nextInt(1000000, 9999999);
        return "+375" + operator + String.format("%07d", subscriber);
    }

    @Transactional
    private void generateFakeCalls(List<FakeUserData> users) {
        Random random = new Random();
        BigDecimal pricePerMinute = BigDecimal.valueOf(0.10); // 0.10 per minute

        // Get all active users to generate calls between them
        List<User> allUsers = userService.findAllActive();

        for (int i = 0; i < allUsers.size(); i++) {
            for (int j = 0; j < allUsers.size(); j++) {
                if (i == j) continue;

                User caller = allUsers.get(i);
                User callee = allUsers.get(j);

                // Get primary phones using service to avoid lazy loading issues
                Optional<PhoneNumber> callerPhoneOpt = phoneNumberService.getPrimaryPhoneForUser(caller);
                Optional<PhoneNumber> calleePhoneOpt = phoneNumberService.getPrimaryPhoneForUser(callee);

                if (callerPhoneOpt.isEmpty() || calleePhoneOpt.isEmpty()) {
                    // Silently skip users without primary phone - this is expected for admin or users without phones
                    continue;
                }

                PhoneNumber callerPhone = callerPhoneOpt.get();
                PhoneNumber calleePhone = calleePhoneOpt.get();

                // Generate 1-2 calls per pair to reduce volume
                int numCalls = 1 + random.nextInt(2);
                for (int k = 0; k < numCalls; k++) {
                    // Random calls: some incoming, some outgoing
                    CallType callType = random.nextBoolean() ? CallType.OUTGOING : CallType.INCOMING;
                    long durationSeconds = 60 + random.nextInt(300); // 1-5 minutes

                    try {
                        callService.createCall(callerPhone, calleePhone, callType, durationSeconds, pricePerMinute);
                    } catch (Exception e) {
                        System.err.println("Failed to create call between " + caller.getUsername() + " and " + callee.getUsername() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
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
        String publicContactInfo;
        User user;

        FakeUserData(String username, String password, String firstName, String lastName, String middleName, String phone, String publicContactInfo) {
            this.username = username;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.middleName = middleName;
            this.phone = phone;
            this.publicContactInfo = publicContactInfo;
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