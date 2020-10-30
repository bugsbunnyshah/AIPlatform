package io.quarkus.vault.runtime;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.credentials.CredentialsProvider;
import io.quarkus.vault.VaultException;
import io.quarkus.vault.runtime.config.CredentialsProviderConfig;
import io.quarkus.vault.runtime.config.VaultRuntimeConfig;

public class VaultCredentialsProvider implements CredentialsProvider {

    private VaultKvManager vaultKvManager;
    private VaultDbManager vaultDbManager;
    private VaultRuntimeConfig serverConfig;

    public VaultCredentialsProvider(VaultRuntimeConfig serverConfig, VaultKvManager vaultKvManager,
            VaultDbManager vaultDbManager) {
        this.serverConfig = serverConfig;
        this.vaultKvManager = vaultKvManager;
        this.vaultDbManager = vaultDbManager;
    }

    @Override
    public Map<String, String> getCredentials(String credentialsProviderName) {

        CredentialsProviderConfig config = serverConfig.credentialsProvider.get(credentialsProviderName);

        if (config == null) {
            throw new VaultException("unknown credentials provider with name " + credentialsProviderName);
        }

        if (config.databaseCredentialsRole.isPresent()) {
            return vaultDbManager.getDynamicDbCredentials(config.databaseCredentialsRole.get());
        }

        if (config.kvPath.isPresent()) {
            String password = vaultKvManager.readSecret(config.kvPath.get()).get(config.kvKey);
            Map<String, String> result = new HashMap<>();
            result.put(PASSWORD_PROPERTY_NAME, password);
            return result;
        }

        throw new VaultException(
                "one of database-credentials-role or kv-path is required on credentials provider " + credentialsProviderName);

    }

}
