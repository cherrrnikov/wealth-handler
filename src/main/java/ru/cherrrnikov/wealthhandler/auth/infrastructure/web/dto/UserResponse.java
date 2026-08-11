package ru.cherrrnikov.wealthhandler.auth.infrastructure.web.dto;
import java.util.List;

public record UserResponse(String email, String username, List<String> roles) {
}
