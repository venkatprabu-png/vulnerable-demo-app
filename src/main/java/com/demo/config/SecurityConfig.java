package com.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

/**
 * Security configuration using Spring Security 5.6.2.
 *
 * Vulnerable to:
 *   CVE-2023-34062 — Spring Security Authorization Bypass (fix: 5.8.8+)
 *   CVE-2023-34034 — Spring Security WebFlux path matching bypass (fix: 5.8.8+)
 *
 * No code change is needed — just upgrade spring-security-web and
 * spring-security-config to 5.8.8+ in pom.xml.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/health", "/actuator/health").permitAll()
                .antMatchers("/api/**").authenticated()
            .and()
            .httpBasic();
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin123")
                .roles("ADMIN")
                .build(),
            User.withDefaultPasswordEncoder()
                .username("user")
                .password("user123")
                .roles("USER")
                .build()
        );
    }
}
