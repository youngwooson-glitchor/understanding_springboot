package me.shinsunyoung.springbootdeveloper.config;

import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toH2Console;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    // disable spring security feature
    @Bean
    public WebSecurityCustomizer configure() {
        return (web) -> web.ignoring().requestMatchers(toH2Console()).requestMatchers("/static/**");
    }

    // configuration web based security that requested by especially http request
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(auth -> auth // authentication settings
                .requestMatchers("/login", "/signup", "/user").permitAll().anyRequest()
                .authenticated()).formLogin(formLogin -> formLogin // formbased login
                        .loginPage("/login").defaultSuccessUrl("/articles"))
                .logout(logout -> logout // logout settings
                        .logoutSuccessUrl("/login").invalidateHttpSession(true))
                .csrf(AbstractHttpConfigurer::disable) // disabled csrf
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
