package ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import ru.cherrrnikov.wealthhandler.auth.domain.Role;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.persistence.entity.RoleEntity;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleEntity toEntity(Role role);
    Role toDomain(RoleEntity role);
}
