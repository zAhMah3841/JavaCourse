package com.example.call_track.spec;

import com.example.call_track.entity.call.Call;
import com.example.call_track.entity.call.CallType;
import com.example.call_track.entity.PhoneNumber;
import com.example.call_track.entity.user.User;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class CallSpecifications {
    public static Specification<Call> filterAll(
            User user,
            String name,
            String myNumbers,
            String phone,
            String callType,
            String startDate,
            String endDate,
            BigDecimal minCost,
            BigDecimal maxCost,
            BigDecimal pricePerMinute,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<Call, PhoneNumber> callerPhone = root.join("callerPhone");
            Join<Call, PhoneNumber> calleePhone = root.join("calleePhone");
            Join<PhoneNumber, User> callerUser = callerPhone.join("user");
            Join<PhoneNumber, User> calleeUser = calleePhone.join("user");

            // Фильтр: текущий пользователь — или вызывающий, или вызываемый (только если не админ)
            if (user != null) {
                predicates.add(cb.or(
                        cb.equal(callerUser, user),
                        cb.equal(calleeUser, user)
                ));
            }

            // Фильтр по имени (ищет по имени, фамилии и отчеству оппонента или всех для админа)
            if (name != null && !name.isBlank()) {
                String likeName = "%" + name.trim().toLowerCase() + "%";
                if (user != null) {
                    // Для обычного пользователя — только оппонент
                    Predicate otherPartyMatchCaller = cb.and(
                            cb.notEqual(callerUser, user),
                            cb.or(
                                    cb.like(cb.lower(callerUser.get("firstName")), likeName),
                                    cb.like(cb.lower(callerUser.get("lastName")), likeName),
                                    cb.like(cb.lower(callerUser.get("middleName")), likeName)
                            ));
                    Predicate otherPartyMatchCallee = cb.and(
                            cb.notEqual(calleeUser, user),
                            cb.or(
                                    cb.like(cb.lower(calleeUser.get("firstName")), likeName),
                                    cb.like(cb.lower(calleeUser.get("lastName")), likeName),
                                    cb.like(cb.lower(calleeUser.get("middleName")), likeName)
                            ));
                    predicates.add(cb.or(otherPartyMatchCaller, otherPartyMatchCallee));
                } else {
                    // Для админа — поиск по всем участникам звонка
                    Predicate callerMatch = cb.or(
                            cb.like(cb.lower(callerUser.get("firstName")), likeName),
                            cb.like(cb.lower(callerUser.get("lastName")), likeName),
                            cb.like(cb.lower(callerUser.get("middleName")), likeName)
                    );
                    Predicate calleeMatch = cb.or(
                            cb.like(cb.lower(calleeUser.get("firstName")), likeName),
                            cb.like(cb.lower(calleeUser.get("lastName")), likeName),
                            cb.like(cb.lower(calleeUser.get("middleName")), likeName)
                    );
                    predicates.add(cb.or(callerMatch, calleeMatch));
                }
            }

            // Фильтр по номерам (показывать звонки, где номер пользователя в списке выбранных) — только для обычных пользователей
            if (user != null && myNumbers != null && !myNumbers.isBlank()) {
                List<String> nums = Arrays.stream(myNumbers.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                if (!nums.isEmpty()) {
                    // Показывать звонки, где пользовательский номер (caller или callee) в выбранных
                    Predicate outgoingMatch = cb.and(cb.equal(callerUser, user), callerPhone.get("phone").in(nums));
                    Predicate incomingMatch = cb.and(cb.equal(calleeUser, user), calleePhone.get("phone").in(nums));
                    predicates.add(cb.or(outgoingMatch, incomingMatch));
                }
            }

            // Фильтр по телефону оппонента (абонента) или всем для админа
            if (phone != null && !phone.isBlank()) {
                String likePhone = "%" + phone.trim() + "%";
                if (user != null) {
                    Predicate callerIsOther = cb.and(cb.notEqual(callerUser, user), cb.like(callerPhone.get("phone"), likePhone));
                    Predicate calleeIsOther = cb.and(cb.notEqual(calleeUser, user), cb.like(calleePhone.get("phone"), likePhone));
                    predicates.add(cb.or(callerIsOther, calleeIsOther));
                } else {
                    // Для админа — поиск по любому телефону в звонке
                    predicates.add(cb.or(cb.like(callerPhone.get("phone"), likePhone), cb.like(calleePhone.get("phone"), likePhone)));
                }
            }

            // Тип звонка (входящий или исходящий) — только для обычных пользователей
            if (user != null && callType != null && !callType.isBlank()) {
                if (callType.equals("OUTGOING")) {
                    predicates.add(cb.equal(callerUser, user));
                } else if (callType.equals("INCOMING")) {
                    predicates.add(cb.equal(calleeUser, user));
                }
            }

            // Диапазон дат
            if (startDate != null && !startDate.isBlank()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("callDateTime"), start));
                } catch (DateTimeParseException ignore) {}
            }
            if (endDate != null && !endDate.isBlank()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate, java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    predicates.add(cb.lessThanOrEqualTo(root.get("callDateTime"), end));
                } catch (DateTimeParseException ignore) {}
            }

            // Стоимость звонка
            if (minCost != null) predicates.add(cb.greaterThanOrEqualTo(root.get("totalCost"), minCost));
            if (maxCost != null) predicates.add(cb.lessThanOrEqualTo(root.get("totalCost"), maxCost));
            // Цена за минуту
            if (pricePerMinute != null)
                predicates.add(cb.equal(root.get("pricePerMinute"), pricePerMinute));
            if (minPrice != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("pricePerMinute"), minPrice));
            if (maxPrice != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("pricePerMinute"), maxPrice));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Sort buildSort(String sortBy, String sortDir) {
        String field = "callDateTime";
        if (sortBy == null || sortBy.isBlank()) sortBy = "date";
        if (sortDir == null || sortDir.isBlank()) sortDir = "desc";

        switch (sortBy) {
            case "cost":
                field = "totalCost";
                break;
            case "duration":
                field = "durationSeconds";
                break;
            case "price":
                field = "pricePerMinute";
                break;
            case "date":
            default:
                field = "callDateTime";
        }
        return sortDir.equalsIgnoreCase("asc") ? Sort.by(field).ascending() : Sort.by(field).descending();
    }
}
