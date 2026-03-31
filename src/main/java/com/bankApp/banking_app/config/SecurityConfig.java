package com.bankApp.banking_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 1. Disable CSRF because we are using stateless REST APIs
            .csrf(csrf -> csrf.disable())
            
            // 2. Enable CORS with our custom configuration defined below
            .cors(Customizer.withDefaults()) 
            
            // 3. Define which URLs are public and which are protected
            .authorizeHttpRequests(auth -> auth
                // Allow static resources and landing pages
                .requestMatchers("/", "/pages/**", "/images/**").permitAll()
                
                // Allow all Authentication endpoints (Login & Register)
                .requestMatchers("/api/auth/**").permitAll()
                
                // CRITICAL: Allow Account endpoints so Dashboard can load without login errors
                .requestMatchers("/api/v1/accounts/**").permitAll() 
                
                // Allow Swagger UI for testing API documentation
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Any other request must be authenticated
                .anyRequest().authenticated()
            )
            
            // 4. Use Basic Auth and disable the default redirecting Login Form
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form.disable());

        return http.build();
    }

    /**
     * CORS Configuration Bean
     * This solves the "Blocked by CORS policy" error by telling the browser 
     * that our Backend accepts requests from the Frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins (This fixes the 'origin null' error from local files)
        configuration.setAllowedOrigins(Arrays.asList("*")); 
        
        // Allow common HTTP methods used in our Banking App
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allow necessary headers for JSON communication and Auth
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept"));
        
        // Must be false when using "*" for AllowedOrigins
        configuration.setAllowCredentials(false); 

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all paths in our application
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Password Encoder Bean
     * Uses BCrypt to securely hash passwords before saving them to the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
