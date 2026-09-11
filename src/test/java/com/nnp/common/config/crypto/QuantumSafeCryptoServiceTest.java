package com.nnp.common.config.crypto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nnp.common.config.crypto.dto.QuantumDecryptResponse;
import com.nnp.common.config.crypto.dto.QuantumEncryptResponse;
import com.nnp.common.config.exception.ConfigServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import org.springframework.beans.factory.annotation.Value;

class QuantumSafeCryptoServiceTest {

    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;
    @Value("${quantum.safe.service.url}")
    private String serviceUrl = "http://localhost:8080";
    private QuantumSafeCryptoService cryptoService;

    @BeforeEach
    void setUp() {
        restTemplate = Mockito.mock(RestTemplate.class);
        objectMapper = new ObjectMapper();
        cryptoService = new QuantumSafeCryptoService(serviceUrl, restTemplate, objectMapper);
    }

    @Test
    void testEncrypt_Success() {
        QuantumEncryptResponse mockResp = new QuantumEncryptResponse(
                "Hybrid: X25519 + ML-KEM-768 + HKDF-SHA256 + AES-256-GCM",
                "kem-ct-base64",
                "x25519-pub-base64",
                "nonce-base64",
                "ciphertext-base64"
        );

        when(restTemplate.postForEntity(
                eq("http://localhost:8080/encrypt"),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(QuantumEncryptResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResp, HttpStatus.OK));

        String encrypted = cryptoService.encrypt("mySecretPassword");

        assertNotNull(encrypted);
        assertTrue(cryptoService.isEncryptedPayload(encrypted));
        assertTrue(encrypted.contains("kem-ct-base64"));
        assertTrue(encrypted.contains("x25519-pub-base64"));
        assertTrue(encrypted.contains("nonce-base64"));
        assertTrue(encrypted.contains("ciphertext-base64"));
        verify(restTemplate, times(1)).postForEntity(eq("http://localhost:8080/encrypt"), any(), eq(QuantumEncryptResponse.class));
    }

    @Test
    void testEncrypt_AvoidDoubleEncryption() {
        String existingEncryptedPayload = "{\"kem_ciphertext\":\"ct\",\"x25519_ephemeral_public\":\"pub\",\"nonce\":\"n\",\"ciphertext\":\"c\"}";

        String result = cryptoService.encrypt(existingEncryptedPayload);

        assertEquals(existingEncryptedPayload, result);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testEncrypt_NullReturnsNull() {
        assertNull(cryptoService.encrypt(null));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testEncrypt_ServiceFailureThrowsException() {
        when(restTemplate.postForEntity(
                eq("http://localhost:8080/encrypt"),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(QuantumEncryptResponse.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        ConfigServiceException ex = assertThrows(ConfigServiceException.class, () -> {
            cryptoService.encrypt("plaintext");
        });

        assertTrue(ex.getMessage().contains("Quantum-safe encryption failed"));
    }

    @Test
    void testDecrypt_Success() {
        String encryptedPayload = "{\"kem_ciphertext\":\"kem-ct\",\"x25519_ephemeral_public\":\"x-pub\",\"nonce\":\"nonce\",\"ciphertext\":\"ct\"}";
        QuantumDecryptResponse mockResp = new QuantumDecryptResponse("mySecretPassword");

        when(restTemplate.postForEntity(
                eq("http://localhost:8080/decrypt"),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(QuantumDecryptResponse.class)))
                .thenReturn(new ResponseEntity<>(mockResp, HttpStatus.OK));

        String decrypted = cryptoService.decrypt(encryptedPayload);

        assertEquals("mySecretPassword", decrypted);
        verify(restTemplate, times(1)).postForEntity(eq("http://localhost:8080/decrypt"), any(), eq(QuantumDecryptResponse.class));
    }

    @Test
    void testDecrypt_PlaintextReturnedAsIs() {
        String plaintext = "ordinary-plaintext-value";

        String result = cryptoService.decrypt(plaintext);

        assertEquals(plaintext, result);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testDecrypt_NullReturnsNull() {
        assertNull(cryptoService.decrypt(null));
        verifyNoInteractions(restTemplate);
    }

    @Test
    void testDecrypt_ServiceFailureThrowsException() {
        String encryptedPayload = "{\"kem_ciphertext\":\"kem-ct\",\"x25519_ephemeral_public\":\"x-pub\",\"nonce\":\"nonce\",\"ciphertext\":\"ct\"}";

        when(restTemplate.postForEntity(
                eq("http://localhost:8080/decrypt"),
                ArgumentMatchers.<HttpEntity<?>>any(),
                eq(QuantumDecryptResponse.class)))
                .thenThrow(new RuntimeException("Decryption error"));

        ConfigServiceException ex = assertThrows(ConfigServiceException.class, () -> {
            cryptoService.decrypt(encryptedPayload);
        });

        assertTrue(ex.getMessage().contains("Quantum-safe decryption failed"));
    }

    @Test
    void testIsEncryptedPayload() {
        assertFalse(cryptoService.isEncryptedPayload(null));
        assertFalse(cryptoService.isEncryptedPayload(""));
        assertFalse(cryptoService.isEncryptedPayload("postgres"));
        assertFalse(cryptoService.isEncryptedPayload("{\"foo\":\"bar\"}"));
        assertTrue(cryptoService.isEncryptedPayload(
                "{\"kem_ciphertext\":\"k\",\"x25519_ephemeral_public\":\"p\",\"nonce\":\"n\",\"ciphertext\":\"c\"}"));
    }
}
