package com.springboot.roadready.config;

import com.springboot.roadready.service.MyUserDetailsService;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final MyUserDetailsService myUserDetailsService;
    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()

                        // login
                        .requestMatchers(HttpMethod.GET, "/api/auth/login").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/auth/user-details").authenticated()

                        // register
                        .requestMatchers(HttpMethod.POST, "/api/customer/add").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/admin/add").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/rentalagent/add").permitAll()

                        // brands and locations: everyone signed in can read them, only admin adds
                        .requestMatchers(HttpMethod.GET, "/api/brand/all", "/api/location/all").hasAnyAuthority("CUSTOMER", "ADMIN", "RENTAL_AGENT")
                        .requestMatchers(HttpMethod.POST, "/api/brand/add").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/location/add").hasAuthority("ADMIN")

                        // cars
                        .requestMatchers(HttpMethod.POST, "/api/car/add").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/car/{id}/upload").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/car/{id}").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/car/all", "/api/car/filter", "/api/car/{id}").hasAnyAuthority("CUSTOMER", "ADMIN", "RENTAL_AGENT")

                        // rental agent's own cars
                        .requestMatchers("/api/car/api/rental-agent/**").hasAuthority("RENTAL_AGENT")

                        // customer reservations
                        .requestMatchers(HttpMethod.POST, "/api/reservation/add").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/reservation/my").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/reservation/check-availability").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/reservation/{id}/modify").hasAuthority("CUSTOMER")
                        // customer asks for a cancellation
                        .requestMatchers(HttpMethod.PUT, "/api/reservation/{id}/cancel").hasAuthority("CUSTOMER")

                        // admin reservations
                        .requestMatchers(HttpMethod.GET, "/api/reservation/admin/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/reservation/cancellation-requests").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/reservation/{id}/status").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/reservation/{id}/cancellation/approve").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/reservation/{id}/cancellation/reject").hasAuthority("ADMIN")

                        // check in, check out, damage charge
                        .requestMatchers(HttpMethod.PUT, "/api/reservation/{id}/check-in").hasAuthority("RENTAL_AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/reservation/{id}/check-out").hasAuthority("RENTAL_AGENT")
                        .requestMatchers(HttpMethod.PUT, "/api/reservation/{id}/damage-charge").hasAuthority("RENTAL_AGENT")

                        // payments: customers create and read their own, admin only reads completed ones
                        .requestMatchers(HttpMethod.POST, "/api/payment/add").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/payment/my").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/admin/payment/all").hasAuthority("ADMIN")

                        // reviews: everyone can read, only customers write or check eligibility
                        .requestMatchers(HttpMethod.POST, "/api/review/add").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/review/car/{id}/eligibility").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/review/car/{id}", "/api/review/car/{id}/summary").permitAll()

                        // dashboards
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/summary").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/customer/dashboard/summary").hasAuthority("CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/rentalagent/dashboard/summary").hasAuthority("RENTAL_AGENT")

                        // admin: users and rental agents
                        .requestMatchers(HttpMethod.GET, "/api/admin/user/all").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/admin/user/{userId}/status").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/rental-agent/all").hasAuthority("ADMIN")

                        .anyRequest().authenticated()
                );


        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.httpBasic(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public AuthenticationProvider getProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider(myUserDetailsService);
        authenticationProvider.setPasswordEncoder(getPasswordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

