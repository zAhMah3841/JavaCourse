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

            // Фильтр: текущий пользователь — или вызывающий, или вызываемый
            predicates.add(cb.or(
                    cb.equal(callerUser, user),
                    cb.equal(calleeUser, user)
            ));

            // Фильтр по имени (ищет по имени, фамилии и отчеству оппонента)
            if (name != null && !name.isBlank()) {
                String likeName = "%" + name.trim().toLowerCase() + "%";
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
            }

            // Фильтр по номерам (userPhone среди выбранных номеров)
            if (myNumbers != null && !myNumbers.isBlank()) {
                List<String> nums = Arrays.stream(myNumbers.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
                if (!nums.isEmpty()) {
                    predicates.add(cb.or(
                            callerPhone.get("phone").in(nums),
                            calleePhone.get("phone").in(nums)
                    ));
                }
            }

            // Фильтр по телефону оппонента (абонента)
            if (phone != null && !phone.isBlank()) {
                Predicate callerIsOther = cb.and(cb.notEqual(callerUser, user), cb.equal(callerPhone.get("phone"), phone));
                Predicate calleeIsOther = cb.and(cb.notEqual(calleeUser, user), cb.equal(calleePhone.get("phone"), phone));
                predicates.add(cb.or(callerIsOther, calleeIsOther));
            }

            // Тип звонка (входящий или исходящий)
            if (callType != null && !callType.isBlank()) {
                if (callType.equals("OUTGOING")) {
                    predicates.add(cb.equal(callerUser, user));
                } else if (callType.equals("INCOMING")) {
                    predicates.add(cb.equal(calleeUser, user));
                }
            }

            // Диапазон дат
            if (startDate != null && !startDate.isBlank()) {
                try {
                    LocalDateTime start = LocalDateTime.parse(startDate.replace("T", " ").replace(" ", "T"));
                    predicates.add(cb.greaterThanOrEqualTo(root.get("callDateTime"), start));
                } catch (DateTimeParseException ignore) {}
            }
            if (endDate != null && !endDate.isBlank()) {
                try {
                    LocalDateTime end = LocalDateTime.parse(endDate.replace("T", " ").replace(" ", "T"));
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
