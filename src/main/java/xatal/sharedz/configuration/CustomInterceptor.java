package xatal.sharedz.configuration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerMapping;
import xatal.sharedz.security.TokenUtils;

import java.io.IOException;
import java.util.Map;

@Component
public class CustomInterceptor implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!this.tokenValid(request)) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.sendError(HttpStatus.NOT_ACCEPTABLE.value());
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean tokenValid(ServletRequest request) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (!this.isLogin(httpRequest) && !this.isPostUsuario(httpRequest)) {
            String token = httpRequest.getHeader("Token");
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

    private boolean usernameMatchesToken(HttpServletRequest request, String token) {
        Map pathVars = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        return pathVars.containsKey("username")
                && TokenUtils.getTokenClaims(token).get("username").equals(pathVars.get("username"));
    }
}
