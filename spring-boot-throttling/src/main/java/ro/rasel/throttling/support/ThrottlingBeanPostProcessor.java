package ro.rasel.throttling.support;

import ro.rasel.throttling.Throttling;
import ro.rasel.throttling.ThrottlingException;
import ro.rasel.throttling.service.ThrottlingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor}
 * implementation that facilitates {@link Throttling} configuration
 * and decides whether a method invocation is allowed or not.
 * In case method reaches {@link Throttling} configuration limit
 * {@link ThrottlingException} is thrown.
 *
 * @author Nikolay Papakha (nikolay.papakha@gmail.com)
 */
public class ThrottlingBeanPostProcessor implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(BeanPostProcessor.class);

    private final ThrottlingService throttlingService;
    private final Map<String, Class> beanNamesToOriginalClasses;

    public ThrottlingBeanPostProcessor(ThrottlingService throttlingService) {
        this.throttlingService = throttlingService;
        beanNamesToOriginalClasses = new HashMap<>();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        for (Annotation annotation : bean.getClass().getAnnotations()) {
            // do not wrap sprint controllers with a proxy
            if (Controller.class.isInstance(annotation) || RestController.class.isInstance(annotation)) {
                return bean;
            }
        }

        for (Method method : bean.getClass().getMethods()) {
            Throttling annotation = method.getAnnotation(Throttling.class);
            if (annotation != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("discovered bean '" + beanName + "' annotated with @Throttling");
                }
                beanNamesToOriginalClasses.put(beanName, bean.getClass());
                break;
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

        Class<?> clazz = beanNamesToOriginalClasses.get(beanName);
        if (clazz == null) {
            return bean;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("replacing bean '" + beanName + "' with a proxy");
        }

        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), (proxy, method, args) -> {

            Throttling annotation =
                    findAnnotation(clazz.getMethod(method.getName(), method.getParameterTypes()), Throttling.class);

            if (annotation != null) {
                throttlingService.throttle(bean, clazz, method, args, annotation);
            }

            // call original method
            return ReflectionUtils.invokeMethod(method, bean, args);
        });
    }

}
