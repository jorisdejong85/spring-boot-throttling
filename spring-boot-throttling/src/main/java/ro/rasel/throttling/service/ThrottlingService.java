package ro.rasel.throttling.service;

import ro.rasel.throttling.Throttling;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

public interface ThrottlingService {
    void throttle(Object bean, Class<?> clazz, Method method, Object[] args, Throttling throttling);

    void throttle(
            HandlerMethod handlerMethod, Object bean, Class<?> clazz, Method method,
            Object[] methodParameters, Throttling throttling, String requestURI);
}
