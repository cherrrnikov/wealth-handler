package ru.cherrrnikov.wealthhandler.auth.domain;

import lombok.*;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;

    private String email;

    private String passwordHash;

    private String username;

    private Instant createdAt;

    private Instant updatedAt;

    private Set<Role> roles;
}
