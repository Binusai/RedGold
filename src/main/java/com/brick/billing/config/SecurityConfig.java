package com.brick.billing.config;

import com.brick.billing.model.User;
import com.brick.billing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.userdetails.User.UserBuilder;

@Configuration
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/index.html",
                        "/login",
                        "/login.html",
                        "/perform_login",
                        "/register",
                        "/images/**",
                        "/logo.png",
                        "/logo1.png",
                        "/css/**",
                        "/js/**",
                        "/static/**",
                        "/api/**",
                        "/api/report/save-draft",  
                        "/api/report/finalize",     
                        "/api/report/by-booking/**", 
                        "/home.html",
                        "/add-booking.html",
                        "/history.html",
                        "/reports.html",
                        "/report.html",
                        "/revenue.html",
                        "/bookings.html",
                        "/create-investment.html",
                        "/view-investment.html",
                        "/manifest.json",
                        "/service-worker.js"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(login -> login.disable())
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder encoder) {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                
                UserBuilder builder = org.springframework.security.core.userdetails.User.builder();
                builder.username(user.getUsername())
                       .password(user.getPassword())
                       .roles("USER");
                
                return builder.build();
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
