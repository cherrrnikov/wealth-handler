package ru.cherrrnikov.wealthhandler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.cherrrnikov.wealthhandler.auth.infrastructure.security.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class WealthHandlerApplication {
    public static void main(String[] args) {
        SpringApplication.run(WealthHandlerApplication.class, args);
    }
}
