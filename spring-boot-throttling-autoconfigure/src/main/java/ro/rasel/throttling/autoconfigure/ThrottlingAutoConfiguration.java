package ro.rasel.throttling.autoconfigure;

import ro.rasel.throttling.service.ThrottlingDataService;
import ro.rasel.throttling.service.ThrottlingDataServiceImpl;
import ro.rasel.throttling.service.ThrottlingEvaluator;
import ro.rasel.throttling.service.ThrottlingEvaluatorImpl;
import ro.rasel.throttling.service.ThrottlingService;
import ro.rasel.throttling.service.ThrottlingServiceImpl;
import ro.rasel.throttling.support.ThrottlingBeanPostProcessor;
import ro.rasel.throttling.support.ThrottlingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ConditionalOnClass(ThrottlingBeanPostProcessor.class)
@EnableConfigurationProperties(ThrottlingProperties.class)
public class ThrottlingAutoConfiguration {

    private static final int DEFAULT_LRU_CACHE_CAPACITY = 10000;

    private final ThrottlingProperties throttlingProperties;

    @Autowired
    public ThrottlingAutoConfiguration(ThrottlingProperties throttlingProperties) {
        this.throttlingProperties = throttlingProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingBeanPostProcessor throttlingBeanPostProcessor() {
        return new ThrottlingBeanPostProcessor(throttlingService());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public ThrottlingInterceptor throttlingInterceptor() {
        return new ThrottlingInterceptor(throttlingService());
    }

    @Bean
    @ConditionalOnWebApplication
    public WebMvcConfigurer interceptorAdapter() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(throttlingInterceptor());
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingService throttlingService() {
        return new ThrottlingServiceImpl(throttlingEvaluator(), throttlingDataService());
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingEvaluator throttlingEvaluator() {
        return new ThrottlingEvaluatorImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingDataService throttlingDataService() {
        return new ThrottlingDataServiceImpl(throttlingProperties.getLruCacheCapacity() != null ?
                throttlingProperties.getLruCacheCapacity() : DEFAULT_LRU_CACHE_CAPACITY);
    }

}
