package com.peernet.service;

import com.peernet.config.ApiException;
import com.peernet.model.AppSetting;
import com.peernet.repository.AppSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Admin-editable settings: matching weights, escalation time and helpers per round. */
@Service
@RequiredArgsConstructor
public class SettingsService {
    public static final Map<String, String> DEFAULTS = new LinkedHashMap<>();
    static {
        DEFAULTS.put("w_topic", "0.40");
        DEFAULTS.put("w_reputation", "0.25");
        DEFAULTS.put("w_availability", "0.20");
        DEFAULTS.put("w_responsiveness", "0.15");
        DEFAULTS.put("escalation_minutes", "30");
        DEFAULTS.put("helpers_per_round", "3");
    }

    private final AppSettingRepository repository;

    public double getDouble(String key) {
        String value = repository.findById(key).map(AppSetting::getVal).orElse(DEFAULTS.get(key));
        return Double.parseDouble(value);
    }

    public Map<String, String> all() {
        Map<String, String> result = new LinkedHashMap<>();
        DEFAULTS.forEach((k, v) -> result.put(k, repository.findById(k).map(AppSetting::getVal).orElse(v)));
        return result;
    }

    public Map<String, String> update(Map<String, String> changes) {
        changes.forEach((key, value) -> {
            if (!DEFAULTS.containsKey(key)) {
                throw new ApiException(400, "Unknown setting: " + key);
            }
            try {
                double d = Double.parseDouble(value);
                if (d < 0) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                throw new ApiException(400, "Invalid value for " + key);
            }
            AppSetting s = new AppSetting();
            s.setName(key);
            s.setVal(value);
            repository.save(s);
        });
        return all();
    }
}
