package com.weddini.throttling.service;

import com.weddini.throttling.ThrottlingKey;


public interface ThrottlingDataService {

    boolean throttle(ThrottlingKey key, String evaluatedValue);

}
