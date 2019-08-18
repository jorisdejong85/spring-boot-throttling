package ro.rasel.throttling.service;

import ro.rasel.throttling.Throttling;

import java.lang.reflect.Method;


public interface ThrottlingEvaluator {

    String evaluate(Throttling throttlingConfig, Object bean, Class clazz, Method method, Object[] args);

}
