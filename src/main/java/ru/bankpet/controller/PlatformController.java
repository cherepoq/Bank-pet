package ru.bankpet.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
public class PlatformController {
    private final String packageName;
    private final String certificateFingerprint;

    public PlatformController(@Value("${app.android.package-name:app.rentflow.mobile}") String packageName,
            @Value("${app.android.certificate-sha256:REPLACE_WITH_RELEASE_KEY_SHA256}") String certificateFingerprint) {
        this.packageName = packageName;
        this.certificateFingerprint = certificateFingerprint;
    }

    @GetMapping(value = "/.well-known/assetlinks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Map<String, Object>> assetLinks() {
        Map<String, Object> target = Map.of("namespace", "android_app", "package_name", packageName,
                "sha256_cert_fingerprints", List.of(certificateFingerprint));
        return List.of(Map.of("relation", List.of("delegate_permission/common.handle_all_urls"), "target", target));
    }

    @GetMapping(value = "/api/platform/readiness", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> readiness() {
        boolean androidConfigured = certificateFingerprint.matches("(?:[0-9A-Fa-f]{2}:){31}[0-9A-Fa-f]{2}");
        return Map.of("status", androidConfigured ? "READY" : "SETUP_REQUIRED", "androidPackage", packageName,
                "assetLinksConfigured", androidConfigured);
    }
}
