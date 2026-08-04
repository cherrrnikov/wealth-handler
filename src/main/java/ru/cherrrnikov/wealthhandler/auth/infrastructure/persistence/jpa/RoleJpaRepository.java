package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.entity.RoleEntity;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}
