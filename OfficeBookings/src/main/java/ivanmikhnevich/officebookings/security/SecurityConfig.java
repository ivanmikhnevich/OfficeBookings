package ivanmikhnevich.officebookings.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           RSAPublicKey publicKey,
                                           JwtProperties props) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(publicKey, props.issuer(), props.audience());

        http
                .csrf(csrf -> csrf.disable())                 // stateless API
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                // Нет токена / токен невалиден → 401
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        // Токен валиден, но прав не хватает → 403
                        .accessDeniedHandler((req, res, ex) ->
                                res.sendError(HttpStatus.FORBIDDEN.value()))
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}