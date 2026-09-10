package com.nnp.common.config.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnp.common.config.crypto.dto.QuantumDecryptRequest;
import com.nnp.common.config.crypto.dto.QuantumDecryptResponse;
import com.nnp.common.config.crypto.dto.QuantumEncryptRequest;
import com.nnp.common.config.crypto.dto.QuantumEncryptResponse;
import com.nnp.common.config.exception.ConfigServiceException;
import com.nnp.common.config.utils.LogUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.regex.Pattern;

@Service
@Slf4j
public class QuantumSafeCryptoService {

    private static final Pattern PATTERN = Pattern.compile("/+$");
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    @Getter
    private final String serviceUrl;

    @Autowired
    public QuantumSafeCryptoService(
            @Value("${quantum.safe.service.url}") String serviceUrl,
            RestTemplateBuilder restTemplateBuilder) {
        this.serviceUrl = PATTERN.matcher(serviceUrl).replaceAll("");
        this.restTemplate = restTemplateBuilder
                .connectTimeout(Duration.ofSeconds(5))
                .readTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public QuantumSafeCryptoService(String serviceUrl, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.serviceUrl = PATTERN.matcher(serviceUrl).replaceAll("");
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Checks whether a string is already formatted as a Quantum-Safe encrypted JSON payload.
     *
     * @param value the string to check
     * @return true if valid encrypted payload structure, false otherwise
     */
    public boolean isEncryptedPayload(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        String trimmed = value.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return false;
        }
        try {
            QuantumDecryptRequest req = objectMapper.readValue(trimmed, QuantumDecryptRequest.class);
            return req.getKemCiphertext() != null && req.getCiphertext() != null
                    && req.getX25519EphemeralPublic() != null && req.getNonce() != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Encrypts plaintext via the external Quantum-Safe service.
     * Prevents double-encryption if value is already encrypted.
     *
     * @param plaintext plaintext string to encrypt
     * @return JSON string representing the encrypted payload with all crypto parameters
     */
    public String encrypt(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        // Avoid encrypting twice
        if (isEncryptedPayload(plaintext)) {
            log.info("Value is already an encrypted payload, skipping double encryption");
            return plaintext;
        }

        String encryptEndpoint = serviceUrl + "/encrypt";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            QuantumEncryptRequest request = new QuantumEncryptRequest(plaintext);
            HttpEntity<QuantumEncryptRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<QuantumEncryptResponse> response = restTemplate.postForEntity(
                    encryptEndpoint, entity, QuantumEncryptResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ConfigServiceException("Quantum-safe service returned non-success status: " + response.getStatusCode());
            }

            QuantumEncryptResponse encResp = response.getBody();
            // Package into DecryptRequest format for clean storage and subsequent decryption
            QuantumDecryptRequest storePayload = new QuantumDecryptRequest(
                    encResp.getKemCiphertext(),
                    encResp.getX25519EphemeralPublic(),
                    encResp.getNonce(),
                    encResp.getCiphertext()
            );

            return objectMapper.writeValueAsString(storePayload);
        } catch (ConfigServiceException e) {
            log.error("Failed to encrypt configuration value via quantum-safe service at {}: {}", LogUtils.sanitizeForLog(encryptEndpoint), LogUtils.sanitizeForLog(e.getMessage()));
            throw e;
        } catch (Exception e) {
            log.error("Failed to encrypt configuration value via quantum-safe service at {}: {}", LogUtils.sanitizeForLog(encryptEndpoint), LogUtils.sanitizeForLog(e.getMessage()));
            throw new ConfigServiceException("Quantum-safe encryption failed: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypts an encrypted payload JSON via the external Quantum-Safe service.
     * If value is already plaintext (not an encrypted JSON payload), returns it as-is.
     *
     * @param encryptedJson JSON string of the encrypted payload
     * @return decrypted plaintext string
     */
    public String decrypt(String encryptedJson) {
        if (encryptedJson == null) {
            return null;
        }

        if (!isEncryptedPayload(encryptedJson)) {
            log.warn("Value is marked as encrypted but does not have valid payload structure. Returning value as-is.");
            return encryptedJson;
        }

        String decryptEndpoint = serviceUrl + "/decrypt";
        try {
            QuantumDecryptRequest decryptRequest = objectMapper.readValue(encryptedJson, QuantumDecryptRequest.class);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<QuantumDecryptRequest> entity = new HttpEntity<>(decryptRequest, headers);

            ResponseEntity<QuantumDecryptResponse> response = restTemplate.postForEntity(
                    decryptEndpoint, entity, QuantumDecryptResponse.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new ConfigServiceException("Quantum-safe service returned non-success status: " + response.getStatusCode());
            }

            return response.getBody().getPlaintext();
        } catch (ConfigServiceException e) {
            log.error("Failed to decrypt configuration value via quantum-safe service at {}: {}", LogUtils.sanitizeForLog(decryptEndpoint), LogUtils.sanitizeForLog(e.getMessage()));
            throw e;
        } catch (Exception e) {
            log.error("Failed to decrypt configuration value via quantum-safe service at {}: {}", LogUtils.sanitizeForLog(decryptEndpoint),LogUtils.sanitizeForLog( e.getMessage()));
            throw new ConfigServiceException("Quantum-safe decryption failed: " + e.getMessage(), e);
        }
    }

}
