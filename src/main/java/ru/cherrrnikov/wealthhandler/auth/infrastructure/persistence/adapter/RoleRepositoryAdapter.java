package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.RoleRepository;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.jpa.RoleJpaRepository;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.mapper.RoleMapper;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RoleRepositoryAdapter implements RoleRepository {
    private final RoleJpaRepository roleJpaRepository;
    private final RoleMapper roleMapper;


    @Override
    public Optional<Role> findByName(String name) {
        return roleJpaRepository.findByName(name)
                .map(roleMapper::toDomain);
    }
}
