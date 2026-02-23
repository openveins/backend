package xyz.rynav.openveinsapi.interceptors.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.services.JwtService;

import java.util.logging.Logger;


@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtService jwtService;

    private final Logger logger = Logger.getLogger(AuthInterceptor.class.getName());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        AuthRequired authRequired = handlerMethod.getMethodAnnotation(AuthRequired.class);

        if(authRequired == null){
            return true;
        }

        String token = request.getHeader("Authorization");

        logger.info(token);
        if(token == null || !isValidToken(token)) {
            throw new AuthException("Invalid token.", HttpStatus.UNAUTHORIZED);
        }

        if(!jwtService.validateToken(token.split(" ")[1])) {
            throw new AuthException("Invalid token.", HttpStatus.UNAUTHORIZED);
        }


        return true;
    }

    private boolean isValidToken(String token) {
        return token.startsWith("Bearer ") && token.length() > 7;
    }
}
