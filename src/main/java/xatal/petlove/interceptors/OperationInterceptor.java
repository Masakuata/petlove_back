package xatal.petlove.interceptors;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import xatal.petlove.entities.Operation;
import xatal.petlove.repositories.OperationRepository;

import java.io.IOException;

@Component
public class OperationInterceptor implements Filter {
	private final OperationRepository operationRepository;

	public OperationInterceptor(OperationRepository operationRepository) {
		this.operationRepository = operationRepository;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		Operation operation = new Operation(
			this.getOrigin(httpRequest),
			httpRequest.getMethod(),
			httpRequest.getRequestURL().toString()
		);
		this.operationRepository.save(operation);
		filterChain.doFilter(request, response);
	}

	private String getOrigin(HttpServletRequest request) {
		String ipAddress = request.getHeader("X-FORWARDED-FOR");
		if (ipAddress == null) {
			ipAddress = request.getRemoteAddr();
		}
		return ipAddress;
	}
}
