package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.entity.UserEntity;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
