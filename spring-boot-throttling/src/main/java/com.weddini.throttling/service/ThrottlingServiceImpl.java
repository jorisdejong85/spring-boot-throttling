package com.weddini.throttling.service;

import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingException;
import com.weddini.throttling.ThrottlingKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

public class ThrottlingServiceImpl implements ThrottlingService {
    private static final Logger logger = LoggerFactory.getLogger(ThrottlingServiceImpl.class);

    private final ThrottlingEvaluator throttlingEvaluator;
    private final ThrottlingDataService throttlingDataService;

    public ThrottlingServiceImpl(
            ThrottlingEvaluator throttlingEvaluator,
            ThrottlingDataService throttlingDataService) {
        this.throttlingEvaluator = throttlingEvaluator;
        this.throttlingDataService = throttlingDataService;
    }

    @Override
    public void throttle(Object bean, Class<?> clazz, Method method, Object[] args, Throttling throttling) {
        final String evaluatedValue = throttlingEvaluator.evaluate(throttling, bean, clazz, method, args);

        ThrottlingKey key = ThrottlingKey.builder()
                .method(method)
                .throttling(throttling)
                .evaluatedValue(evaluatedValue)
                .build();

        boolean isAllowed = throttlingDataService.throttle(key, evaluatedValue);

        if (!isAllowed) {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot proceed with a method call due to @Throttling configuration, type="
                        + throttling.type() + ", value=" + evaluatedValue);
            }
            throw new ThrottlingException();
        }
    }

    @Override
    public void throttle(
            HandlerMethod handlerMethod, Object bean, Class<?> clazz, Method method,
            Object[] methodParameters, Throttling throttling, String requestURI) {
        String evaluatedValue = throttlingEvaluator.evaluate(throttling, bean, clazz, method, methodParameters);

        ThrottlingKey key = ThrottlingKey.builder()
                .method(handlerMethod.getMethod())
                .throttling(throttling)
                .evaluatedValue(evaluatedValue)
                .build();

        boolean isHandlingAllowed = throttlingDataService.throttle(key, evaluatedValue);

        if (!isHandlingAllowed) {
            if (logger.isDebugEnabled()) {
                logger.debug("cannot proceed with a handling http request [" + requestURI +
                        "] due to @Throttling configuration, type="
                        + throttling.type() + ", value=" + evaluatedValue);
            }
            throw new ThrottlingException();
        }
    }
}
