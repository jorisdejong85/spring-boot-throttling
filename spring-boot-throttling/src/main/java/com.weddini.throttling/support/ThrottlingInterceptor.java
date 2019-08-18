package com.weddini.throttling.support;

import com.weddini.throttling.Throttling;
import com.weddini.throttling.service.ThrottlingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ThrottlingInterceptor extends HandlerInterceptorAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ThrottlingInterceptor.class);

    private final ThrottlingService throttlingService;

    public ThrottlingInterceptor(ThrottlingService throttlingService) {
        this.throttlingService = throttlingService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (HandlerMethod.class.isInstance(handler)) {

            HandlerMethod handlerMethod = (HandlerMethod) handler;

            Throttling annotation = handlerMethod.getMethod().getAnnotation(Throttling.class);

            if (annotation != null) {
                throttlingService.throttle(handlerMethod, handlerMethod.getBean(), handlerMethod.getBeanType(),
                        handlerMethod.getMethod(), handlerMethod.getMethodParameters(), annotation, request.getRequestURI()
                );
            }
        }

        return true;
    }

}
