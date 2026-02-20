package xyz.rynav.openveinsapi.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import xyz.rynav.openveinsapi.models.Config;
import xyz.rynav.openveinsapi.repositories.ConfigRepository;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final ConfigRepository configRepository;
    private static final HashMap<String, String> DEFAULT_SETTINGS = new HashMap<String, String>() {{
        put("cloudflare_turnstile_enabled", "false");
        put("cloudflare_turnstile_siteKey", "1x00000000000000000000AA");
        put("signup_enabled", "false");
        put("onboarding_complete", "false");
    }};

    @Override
    public void run(ApplicationArguments args){
        DEFAULT_SETTINGS.forEach((key, value) -> {
            if(configRepository.findByConfigName(key).isEmpty()) {
                configRepository.save(Config.builder().configName(key).configValue(value).build());
            }
        });
    }

}
