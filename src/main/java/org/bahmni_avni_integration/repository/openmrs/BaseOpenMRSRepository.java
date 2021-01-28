package org.bahmni_avni_integration.repository.openmrs;

import org.springframework.beans.factory.annotation.Value;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public abstract class BaseOpenMRSRepository {
    @Value("${openmrs.uri.prefix}")
    protected String urlPrefix;

    private static String OPENMRS_BASE_PATH = "openmrs/ws/rest/v1";

    protected String getFullPath(String urlPart) {
        try {
            return String.format("%s/%s/%s", urlPrefix, OPENMRS_BASE_PATH, urlPart);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getResourcePath(String resource) {
        return getFullPath(resource);
    }

    protected String getSingleResourcePath(String resource, String uuid) {
        return String.format("%s/%s", getResourcePath(resource), uuid);
    }

    protected String encode(String x) {
        try {
            return URLEncoder.encode(x, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}