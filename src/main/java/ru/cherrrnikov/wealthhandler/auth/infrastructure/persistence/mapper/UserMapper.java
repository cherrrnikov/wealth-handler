package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import ru.cherrrnikov.wealthhandler.auth.domain.User;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserEntity toEntity(User user);
    User toDomain(UserEntity entity);
}
