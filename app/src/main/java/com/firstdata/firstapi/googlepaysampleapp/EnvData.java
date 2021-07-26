package com.firstdata.firstapi.googlepaysampleapp;

import java.util.HashMap;
import java.util.Map;

/**
 *  Prepare for support of multiple environments
 */
public class EnvData {

    private static Map<String, EnvProperties> envMap = new HashMap<>();

    private static class EnvPropertiesImpl implements EnvProperties {

        private String envName;
        private String url;
        private String apiKey;
        private String token;
        private String apiSecret;

        public EnvPropertiesImpl(String envName, String url, String apiKey, String token,
        String apiSecret)
        {
            this.envName = envName;
            this.url = url;
            this.apiKey = apiKey;
            this.token = token;
            this.apiSecret = apiSecret;
        }

        @Override
        public String getEnvName() {
            return envName;
        }

        @Override
        public String getUrl() {
            return url;
        }

        @Override
        public String getApiKey() {
            return apiKey;
        }

        @Override
        public String getToken() {
            return token;
        }

        @Override
        public String getApiSecret() {
            return apiSecret;
        }

    }

    static {
        envMap.put("CERT", new EnvPropertiesImpl(
                        "CERT",
                        "https://cert.api.firstdata.com/gateway/v2/payments",
                        "First Data Api Key",
                        "Issued Token optional for payeezy",
                        "First data api secret"
                )
        );
    }

   public static EnvProperties getProperties(String envName) {
        return envMap.get(envName);
    }
}
