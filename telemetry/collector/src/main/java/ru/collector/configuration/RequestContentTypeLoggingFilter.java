//package ru.collector.configuration;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//
//@Slf4j
//@Component
//public class RequestContentTypeLoggingFilter extends OncePerRequestFilter {
//
//
//    @Override
//    protected void doFilterInternal(
//            HttpServletRequest request,
//            HttpServletResponse response,
//            FilterChain filterChain
//    ) throws ServletException, IOException {
//
//        String uri = request.getRequestURI();
//        if (uri.startsWith("/events")) {
//            log.info("Incoming request: method={}, uri={}, contentType={}, accept={}",
//                    request.getMethod(),
//                    uri,
//                    request.getContentType(),
//                    request.getHeader("Accept"));
//        }
//
//        filterChain.doFilter(request, response);
//    }
//
//}
