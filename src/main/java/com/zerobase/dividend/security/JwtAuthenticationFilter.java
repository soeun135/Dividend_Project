package com.zerobase.dividend.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { //한 요청당 한 번 필터 실행됨.
    public static final String TOKEN_HEADER = "Authorization"; //어떤 키를 기준으로 토큰을 주고받을지
    public static final String TOKEN_PREFIX = "Bearer "; //인증 타입을 나타내기 위해 사용. jwt토큰 사용하는 경우 토큰 앞에 bearer붙임.

    //요청이 날아올 때 http header에 토큰헤더 기준으로해서 value로는 Bearer xxx.xxxx토큰 값으로 오게됨.

    private final TokenProvider tokenProvider;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //요청이 들어올 때마다 요청에 토큰이 포함되어있는지 확인해서 토큰이 유효한지 아닌지 판별

        //request에 헤더에서 토큰을 꺼내올 거임
        String token = this.resolveTokenFromRequest(request);

        if (StringUtils.hasText(token) && this.tokenProvider.validateToken(token)) {
           //토큰을 갖고있따면 && 토큰이 유효하다면
            //->토큰 유효성 검증 완료
            //Security Context에 인증정보를 넣어줄 거임 그렇게 하기 위해서 TokenProvider에 메소드 추가
            Authentication auth = this.tokenProvider.getAuthentication(token);
            //context에 인증정보 넣어줌.
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.info(String.format("[%s] -> %s", this.tokenProvider.getUsername(token), request.getRequestURI()));
        }

        //스프링에는 필터체인이라는 개념이 있어서 필터가 연속적으로 실행될 수 있게 함.
        filterChain.doFilter(request, response);
    }

    private String resolveTokenFromRequest(HttpServletRequest request) {//request에 헤더에서 토큰을 꺼내올 거임
        String token = request.getHeader(TOKEN_HEADER);

        if (!ObjectUtils.isEmpty(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length()); //prefix를 제외한 실제 토큰 부분 반환
        }
        return null;
    }
}
