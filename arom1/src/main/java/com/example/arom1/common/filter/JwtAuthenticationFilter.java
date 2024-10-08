package com.example.arom1.common.filter;

import com.example.arom1.common.exception.BaseException;
import com.example.arom1.common.response.BaseResponse;
import com.example.arom1.common.response.BaseResponseStatus;
import com.example.arom1.common.util.jwt.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //security config 에서 url 설정
//        String requestURI = request.getRequestURI();
//        if (requestURI.equals("/login") || requestURI.equals("/signup") || requestURI.equals("/login/refresh") ) {
//            filterChain.doFilter(request, response);
//            return;
//        }

        System.out.println("JwtAuthenticationFilter 인증 : 진입");

        String token = tokenProvider.resolveToken(request);

        try {
            tokenProvider.validateToken(token);
            Authentication auth = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println("validate success");
            //예외 발생시 doFilter 하면 유저한테 BaseResponse 제공 불가
            filterChain.doFilter(request, response);
        }
        catch (BaseException e) {
            sendErrorResponse(response);
        }
    }

    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        System.out.println("validate failed");
        response.setStatus(BaseResponseStatus.FAIL_TOKEN_AUTHORIZATION.getCode());
        response.setContentType("application/json;charset=UTF-8");

        BaseResponse<String> baseResponse = new BaseResponse<>(BaseResponseStatus.FAIL_TOKEN_AUTHORIZATION);
        String jsonResponse = objectMapper.writeValueAsString(baseResponse);

        response.getWriter().write(jsonResponse);
    }
}
