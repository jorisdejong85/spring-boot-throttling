package ro.rasel.throttling.service;

import ro.rasel.throttling.Throttling;
import ro.rasel.throttling.support.SpElEvaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;

import static ro.rasel.throttling.ThrottlingType.SpEL;


public class ThrottlingEvaluatorImpl implements ThrottlingEvaluator {
    private static final Logger log = LoggerFactory.getLogger(ThrottlingEvaluatorImpl.class);

    private final SpElEvaluator spElEvaluator;

    public ThrottlingEvaluatorImpl() {
        this.spElEvaluator = new SpElEvaluator();
    }

    @Override
    public String evaluate(Throttling throttlingConfig, Object bean, Class clazz, Method method, Object[] args) {
        String value = null;

        if (throttlingConfig.type().equals(SpEL) && !StringUtils.isEmpty(throttlingConfig.expression())) {
            try {
                value = spElEvaluator.evaluate(throttlingConfig.expression(), bean, args, clazz, method);
            } catch (Throwable t) {
                if (log.isErrorEnabled()) {
                    log.error("exception occurred while evaluating SpEl expression = '" +
                            throttlingConfig.expression() + "', please check @Throttling configuration.", t);
                }
            }

        } else {

            HttpServletRequest servletRequest = null;
            try {
                servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            } catch (IllegalStateException e) {
                if (log.isErrorEnabled()) {
                    log.error("no RequestAttributes object is bound to the current thread, " +
                            "please check @Throttling configuration.", e);
                }
            }

            if (servletRequest == null) {
                if (log.isErrorEnabled()) {
                    log.error("cannot find HttpServletRequest in RequestContextHolder while processing " +
                            "@Throttling annotation with type '" + throttlingConfig.type().name() + "'");
                }
            } else {

                switch (throttlingConfig.type()) {
                    case CookieValue:
                        if (!StringUtils.isEmpty(throttlingConfig.cookieName())) {
                            value = Arrays.stream(servletRequest.getCookies())
                                    .filter(c -> c.getName().equals(throttlingConfig.cookieName()))
                                    .findFirst()
                                    .map(Cookie::getValue)
                                    .orElse(null);
                        } else {
                            if (log.isWarnEnabled()) {
                                log.warn("cannot resolve HTTP cookie value for empty cookie name, " +
                                        "please check @Throttling configuration.");
                            }
                        }
                        break;

                    case HeaderValue:
                        if (!StringUtils.isEmpty(throttlingConfig.headerName())) {
                            value = servletRequest.getHeader(throttlingConfig.headerName());
                        } else {
                            if (log.isWarnEnabled()) {
                                log.warn("cannot resolve HTTP header value for empty header name, " +
                                        "please check @Throttling configuration.");
                            }
                        }
                        break;

                    case PrincipalName:
                        if (servletRequest.getUserPrincipal() != null) {
                            value = servletRequest.getUserPrincipal().getName();
                        } else {
                            if (log.isWarnEnabled()) {
                                log.warn("cannot resolve servletRequest.getUserPrincipal().getName() " +
                                        "since servletRequest.getUserPrincipal() is null.");
                            }
                        }
                        break;

                    case RemoteAddr:
                        value = servletRequest.getRemoteAddr();
                        break;
                }
            }
        }

        return value;
    }
}
