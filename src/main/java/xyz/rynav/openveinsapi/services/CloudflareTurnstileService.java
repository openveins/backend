package xyz.rynav.openveinsapi.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import xyz.rynav.openveinsapi.DTOs.Auth.TurnstileResponse;
import xyz.rynav.openveinsapi.exceptions.Auth.AuthException;
import xyz.rynav.openveinsapi.models.Config;
import xyz.rynav.openveinsapi.repositories.ConfigRepository;

import java.util.Optional;

@Service
public class CloudflareTurnstileService {

    private static final String SITEVERIFY_URL = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

    @Value("${turnstile.private_key}")
    private String secretKey;

    private final WebClient webClient = WebClient.create();

    @Autowired
    private ConfigRepository configRepository;

    public boolean verifyCaptchaToken(String token) throws Exception {
        Optional<Config> isTurnstileEnabled = configRepository.findByConfigName("cloudflare_turnstile_enabled");
        Optional<Config> turnstileSitekey = configRepository.findByConfigName("cloudflare_turnstile_siteKey");

        if(isTurnstileEnabled.isEmpty() || turnstileSitekey.isEmpty()) {
            throw new Exception("cloudflare_turnstile_enabled or turnstile_sitekey don't exist in the database!");
        }

        boolean isTurnstileEnabledBool = Boolean.parseBoolean(isTurnstileEnabled.get().getConfigValue());

        String turnstileSiteKeyStr = turnstileSitekey.get().getConfigValue();

        if(isTurnstileEnabledBool && turnstileSiteKeyStr.isBlank()) {
            throw new Exception("cloudflare_turnstile_enabled is true but cloudflare_turnstile_sitekey is empty in the database!");
        }

        boolean isTestEnvironment = turnstileSiteKeyStr.equals("1x00000000000000000000AA")
                || turnstileSiteKeyStr.equals("1x00000000000000000000BB")
                || turnstileSiteKeyStr.equals("3x00000000000000000000FF");

        if(secretKey.isEmpty() && isTurnstileEnabledBool && !isTestEnvironment) {
            throw new Exception("Private key is empty!");
        }

        if(token.isBlank() && !isTurnstileEnabledBool) {
            return true;
        }

        if(token.isBlank() && isTurnstileEnabledBool) {
            throw new AuthException("captcha is missing.");
        }

        if(token.equals("XXXX.DUMMY.TOKEN.XXXX") && isTestEnvironment) {
            return true;
        }

        TurnstileResponse response = validateToken(token);

        if(response == null) {
            throw new Exception("Cloudflare siteverify api failed");
        }

        if(!response.isSuccess()){
            throw new AuthException("Token verification failed");
        }

        return true;

    }

    private TurnstileResponse validateToken(String token){
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("secret", secretKey);
        params.add("response", token);

        return webClient.post()
                .uri(SITEVERIFY_URL)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue(params)
                .retrieve()
                .bodyToMono(TurnstileResponse.class)
                .block();

    }
}
