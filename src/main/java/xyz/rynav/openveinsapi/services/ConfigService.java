package xyz.rynav.openveinsapi.services;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import xyz.rynav.openveinsapi.DTOs.Configs.ConfigPatchRequest;
import xyz.rynav.openveinsapi.DTOs.Configs.ConfigPatchResponse;
import xyz.rynav.openveinsapi.DTOs.Configs.PrivateConfigResponse;
import xyz.rynav.openveinsapi.DTOs.Configs.PublicAuthConfig;
import xyz.rynav.openveinsapi.models.Config;
import xyz.rynav.openveinsapi.repositories.ConfigRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ConfigService {

    @Autowired
    private ConfigRepository configRepository;

    private final Logger logger = LogManager.getLogger(ConfigService.class.getName());


    // Always fail = 2x00000000000000000000AB
    // Always pass = 1x00000000000000000000AA
    // TODO: Improve this function, this is just a quick hack.
    public PublicAuthConfig getPublicAuthConfig(){

        Optional<Config> cloudflareSiteKey = configRepository.findByConfigName("cloudflare_turnstile_siteKey");
        Optional<Config> cloudflareEnabled = configRepository.findByConfigName("cloudflare_turnstile_enabled");
        Optional<Config> signupEnabled = configRepository.findByConfigName("signup_enabled");

        return PublicAuthConfig.builder().turnstileEnabled(cloudflareEnabled.isPresent() && Boolean.parseBoolean(cloudflareEnabled.get().getConfigValue())).turnstileSiteKey(cloudflareSiteKey.isPresent() ? cloudflareSiteKey.get().getConfigValue() : "").message("Success").signupEnabled(signupEnabled.isPresent() && Boolean.parseBoolean(signupEnabled.get().getConfigValue())).build();
    }


    public ConfigPatchResponse patchConfig(@Valid @RequestBody ConfigPatchRequest payload) {

        if(payload.getUpdates() == null ||  payload.getUpdates().isEmpty()){
            throw new ValidationException("Missing the updates field.");
        }

        List<Config> updated = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for(Map.Entry<String, String> entry : payload.getUpdates().entrySet()) {
            Optional<Config> config = configRepository.findByConfigName(entry.getKey());
            if(config.isEmpty()) {
                warnings.add(String.format("Config with name %s not found", entry.getKey()));
            }else{
                String originalValue = config.get().getConfigValue();
                if(!originalValue.equals(entry.getValue())) {
                    config.get().setConfigValue(entry.getValue());
                    Config newconfig = configRepository.save(config.get());
                    updated.add(newconfig);
                }
            }
        }

        return ConfigPatchResponse.builder().warnings(warnings).updated(updated).build();
    }

    public PrivateConfigResponse getConfig(){

        List<Config> configs = configRepository.findAll();

        return PrivateConfigResponse.builder().configList(configs).build();
    }
}
