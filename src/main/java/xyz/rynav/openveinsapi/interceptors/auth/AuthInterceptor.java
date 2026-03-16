package xyz.rynav.openveinsapi.interceptors.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.services.JwtService;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AuthRequired authRequired = handlerMethod.getMethodAnnotation(AuthRequired.class);

        if(authRequired == null){
            return true;
        }

        String token = null;
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for (Cookie cookie : cookies) {
                if(cookie.getName().equals("auth_token")){
                    token = cookie.getValue();
                }
            }
        }else{
            throw new AuthException("Invalid token", HttpStatus.UNAUTHORIZED);
        }

        if(token == null) {
            throw new AuthException("Invalid token.", HttpStatus.UNAUTHORIZED);
        }

        if(!jwtService.validateToken(token)) {
            throw new AuthException("Invalid token.", HttpStatus.UNAUTHORIZED);
        }

        return true;
    }
}
