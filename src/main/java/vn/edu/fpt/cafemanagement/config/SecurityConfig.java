package vn.edu.fpt.cafemanagement.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import vn.edu.fpt.cafemanagement.security.CustomLoginSuccessHandler;
import vn.edu.fpt.cafemanagement.services.CustomOidcUserService;
import vn.edu.fpt.cafemanagement.services.CustomerUserDetailsService;
import vn.edu.fpt.cafemanagement.services.StaffUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomLoginSuccessHandler customLoginSuccessHandler;
    private final CustomerUserDetailsService customerUserDetailsService;
    private final StaffUserDetailsService staffUserDetailsService;

    public SecurityConfig(CustomLoginSuccessHandler customLoginSuccessHandler,
                          CustomerUserDetailsService customerUserDetailsService,
                          StaffUserDetailsService staffUserDetailsService) {
        this.customLoginSuccessHandler = customLoginSuccessHandler;
        this.customerUserDetailsService = customerUserDetailsService;
        this.staffUserDetailsService = staffUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain staffFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/staff/**", "/dashboard/**", "/table/booking/management", "/orders/management", "/order/**", "/product/list**")
                .authenticationProvider(staffAuthenticationProvider())
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/staff/login", "/assets/**").permitAll()
                        .requestMatchers("/dashboard/staff/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard/vouchers/**").hasRole("ADMIN")
                        .requestMatchers("/product/list/**").hasRole("ADMIN")
                        .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "CASHIER")
                        .requestMatchers("/table/booking/management").hasRole("CASHIER")
                        .requestMatchers("/order/create").hasRole("CASHIER")
                        .requestMatchers("/order/edit").hasAnyRole("BARISTA", "WAITER")
                        .requestMatchers("/order/list").hasAnyRole("ADMIN", "CASHIER")
                        .requestMatchers("/order/history-list").hasAnyRole("ADMIN", "CASHIER")
                        .requestMatchers("/order/edit-history").hasAnyRole("ADMIN", "BARISTA", "WAITER")
                        .requestMatchers("/orders/management").hasRole("WAITER")
                        .requestMatchers("/product/edit/**").hasAnyRole("ADMIN", "BARISTA")
                        .requestMatchers("/product/delete/**").hasRole("ADMIN")
                        .anyRequest().authenticated())

                .formLogin(form -> form
                                .loginPage("/staff/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/staff/login?error=true")
                                .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/staff/logout")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessUrl("/staff/login?logout=success")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                .exceptionHandling(exc -> exc
                        .accessDeniedHandler(accessDeniedHandler()));

        return http.build();
    }

    // Filter chain cho Customer
    @Bean
    @Order(2)
    public SecurityFilterChain customerFilterChain(HttpSecurity http, ApplicationContext applicationContext, ProfileCompletionFilter profileCompletionFilter) throws Exception {
        CustomOidcUserService oidcUserService = applicationContext.getBean(CustomOidcUserService.class);

        http
                .securityMatcher("/**")
                .authenticationProvider(customerAuthenticationProvider())
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login**", "/assets/**", "/forgot-password", "/product/{id}",
                                "/set-password**", "/resend-otp", "/verify-email", "/register", "/home", "/customer/login", "/uploads/**", "/api/banners")
                        .permitAll()
                        .anyRequest().authenticated())

                .formLogin(form -> form
                        .loginPage("/customer/login")
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/customer/login?error=true")
                        .permitAll())

                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/customer/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(oidcUserService))
                        .successHandler(customLoginSuccessHandler)
                        .failureUrl("/customer/login?error=true")
                )

                .logout(logout -> logout
                        .logoutUrl("/customer/logout")
                        .addLogoutHandler(new SecurityContextLogoutHandler())
                        .logoutSuccessUrl("/customer/login?logout=success")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())

                .exceptionHandling(exc -> exc
                        .accessDeniedHandler(accessDeniedHandler()));
        http.addFilterAfter(profileCompletionFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider customerAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customerUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public DaoAuthenticationProvider staffAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(staffUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }


    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendRedirect(request.getContextPath() + "/403");
        };
    }


}