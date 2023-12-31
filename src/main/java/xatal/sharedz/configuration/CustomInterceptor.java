package xatal.sharedz.configuration;

import io.jsonwebtoken.Claims;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import xatal.sharedz.security.TokenUtils;

import java.io.IOException;
import java.util.Arrays;

@Component
@Order()
public class CustomInterceptor implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        if (!this.tokenValid(httpRequest)) {
            httpResponse.sendError(HttpStatus.NOT_ACCEPTABLE.value(), "Token is invalid");
        } else if (!this.usernameMatchesToken(httpRequest)) {
            httpResponse.sendError(HttpStatus.NOT_ACCEPTABLE.value(), "Token does not match path username");
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean tokenValid(HttpServletRequest request) {
        if (!this.isLogin(request) && !this.isPostUsuario(request)) {
            String token = request.getHeader("Token");
            return token != null
                    && TokenUtils.getTokenClaims(token) != null
                    && !TokenUtils.isExpired(token);
        }
        return true;
    }

    private boolean isPostUsuario(HttpServletRequest request) {
        return request.getServletPath().contains("usuario")
                && request.getMethod().equals(HttpMethod.POST.name());
    }

    private boolean isLogin(HttpServletRequest request) {
        return request.getServletPath().contains("login");
    }

    private boolean usernameMatchesToken(HttpServletRequest request) {
        String[] pathParts = request.getRequestURI().split("/");
        boolean containsUsuario = Arrays.asList(pathParts).contains("usuario");
        boolean pathLengthIsFour = pathParts.length == 4;
        if (containsUsuario && pathLengthIsFour) {
            String token = request.getHeader("Token");
            if (token == null) {
                return false;
            }
            String pathUsername = pathParts[2];
            Claims tokenClaims = TokenUtils.getTokenClaims(token);
            String tokenUsername = tokenClaims != null ? (String) tokenClaims.get("username") : null;
            return pathUsername.equals(tokenUsername);
        }
        return true;
    }
}
