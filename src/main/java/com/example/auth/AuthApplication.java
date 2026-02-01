package com.example.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.security.Principal;
import java.util.Map;

@SpringBootApplication
@EnableWebSecurity
public class AuthApplication {


    public static void main(String[] args) {
		SpringApplication.run(AuthApplication.class, args);
	}

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JdbcUserDetailsManager jdbcUserDetailsManager(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    UserDetailsPasswordService userDetailsPasswordService(JdbcUserDetailsManager userDetailsManager) {
        return (user, newPassword) -> {
            var updated = User
                    .withUserDetails(user)
                    .password(newPassword)
                    .build();
            userDetailsManager.updateUser(updated);
            return updated;
        };
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(ae -> ae.anyRequest().authenticated())
                .formLogin(Customizer.withDefaults())
                .webAuthn( wa -> wa
                        .allowedOrigins("http://localhost:8080")
                        .rpName("Bootiful Frontend Masters")
                        .rpId("localhost")
                )
                .oneTimeTokenLogin(ott ->
                        ott.tokenGenerationSuccessHandler((request, response, oneTimeToken) -> {

                            System.out.println("please visit http://localhost:8080/login/ott?token=" + oneTimeToken.getTokenValue());


                                response.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
                                response.getWriter().println("You've got the console mail!");
                                response.getWriter().flush();
                        }))
                .build();
    }


}


@Controller
@ResponseBody
class HelloController {


	@GetMapping("/hello")
    Map<String, String> hello(Principal principal) {
        return Map.of("message", "Hello, " + principal.getName() + "!");
    }

}