package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.cherrrnikov.wealthhandler.auth.application.port.out.UserRepository;
import ru.cherrrnikov.wealthhandler.auth.domain.User;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.entity.UserEntity;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.jpa.UserJpaRepository;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.mapper.UserMapper;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Transactional
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity userEntity = userJpaRepository.save(userMapper.toEntity(user));

        return userMapper.toDomain(userEntity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
