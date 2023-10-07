package com.zerobase.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JwtAuthenticationFilter authenticationFilter;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http //jwt는 상태를 가지고 있지 않음.
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                //위에 3줄은 RestAPI로 JWT토큰을 써서 인증방식 구현할 때 붙여줘야하는 부분
                .and()
                .authorizeRequests()
                .antMatchers("/**/signup", "/**/signin").permitAll() //무조건적으로 접근 허용
                //실질적으로 권한 제어가 있는 부분 위 두줄
                .and()
                .addFilterBefore(this.authenticationFilter, UsernamePasswordAuthenticationFilter.class);
                    //스프링에서 정의되어있는 필터. 필터의 순서를 정해준 거임

//        .antMatcher("").hasRole()을 통해 권한 지정이 가능한데
//        이 작업을 어노테이션으로 해주기 위해 @EnableGlobalMethodSecurity 붙인 거임.
    }

    @Override
    public void configure(final WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/h2-console/**");
        //저 경로로 API호출하면 인증정보는 무시하겠다.
        //인증관련 정보 없어도 자유롭게 접근가능.
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
