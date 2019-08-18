package ro.rasel.throttling.example;

import ro.rasel.throttling.Throttling;
import ro.rasel.throttling.ThrottlingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.concurrent.TimeUnit;

@Controller
public class ThrottledController {
    private static final Logger log= LoggerFactory.getLogger(ThrottledController.class);

    /**
     * Throttling configuration:
     * <p>
     * allow 3 HTTP GET requests per minute
     * for each unique {@code javax.servlet.http.HttpServletRequest#getRemoteAddr()}
     */

    @GetMapping("/throttledController")
    @Throttling(limit = 3, timeUnit = TimeUnit.MINUTES, type = ThrottlingType.RemoteAddr)
    public ResponseEntity<String> controllerThrottling() {
        log.info("accessing throttled controller");
        return ResponseEntity.ok().body("ok");
    }

}
