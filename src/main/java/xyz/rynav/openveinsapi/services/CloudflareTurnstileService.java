package xyz.rynav.openveinsapi.services;

import org.springframework.stereotype.Service;

@Service
public class CloudflareTurnstileService {

    public boolean verifyCaptchaToken(String token){
        return true;
    }

}
