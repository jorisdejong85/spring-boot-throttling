package ro.rasel.throttling.service;

import ro.rasel.throttling.ThrottlingKey;


public interface ThrottlingDataService {

    boolean throttle(ThrottlingKey key, String evaluatedValue);

}
