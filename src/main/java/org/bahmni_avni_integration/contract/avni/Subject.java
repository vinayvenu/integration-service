package org.bahmni_avni_integration.contract.avni;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import org.bahmni_avni_integration.util.FormatUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Subject {
    private Map<String, Object> map = new HashMap<>();
//    private static final IgnoredFields ignoredFields = new IgnoredFields("");

    @JsonAnySetter
    public void setMap(final String name, final Object value) {
        map.put(name, value);
    }

    public Object get(String name) {
        return map.get(name);
    }

    public Date getRegistrationDate() {
        String registrationDate = (String) map.get("Registration date");
        return FormatUtil.fromAvniDate(registrationDate);
    }
}