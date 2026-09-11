package com.nnp.common.config.service.impl.jdbc;

import com.nnp.common.config.crypto.QuantumSafeCryptoService;
import com.nnp.common.config.entity.NNPConfig;
import com.nnp.common.config.entity.NNPConfigPK;
import com.nnp.common.config.repo.NNPConfigRepo;
import com.nnp.common.config.to.AppMigrationResultTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JdbcNNPConfigServiceImplTest {

    @Mock
    private NNPConfigRepo configRepo;

    @Mock
    private QuantumSafeCryptoService cryptoService;

    @InjectMocks
    private JdbcNNPConfigServiceImpl configService;

    private NNPConfigPK pk(String app, String profile, String tag, String key) {
        NNPConfigPK p = new NNPConfigPK();
        p.setApplication(app);
        p.setProfile(profile);
        p.setTag(tag);
        p.setKey(key);
        return p;
    }

    private NNPConfig config(String app, String profile, String tag, String key, String value, Boolean isEncrypted) {
        NNPConfig c = new NNPConfig();
        c.setId(pk(app, profile, tag, key));
        c.setValue(value);
        c.setIsEncrypted(isEncrypted);
        return c;
    }

    @Test
    void testGetConfigProperties_MixedPlaintextAndEncrypted() {
        NNPConfig plainConfig = config("app1", "dev", "latest", "DB_HOST", "localhost", false);
        NNPConfig encConfig = config("app1", "dev", "latest", "DB_PASS", "{\"kem_ciphertext\":\"...\"}", true);

        when(configRepo.findConfigForGivenAppAndCommon("app1", "dev", "latest"))
                .thenReturn(List.of(plainConfig, encConfig));
        when(cryptoService.decrypt("{\"kem_ciphertext\":\"...\"}")).thenReturn("decryptedSecret");

        Map<String, String> properties = configService.getConfigProperties("app1", "dev", "latest");

        assertNotNull(properties);
        assertEquals("localhost", properties.get("DB_HOST"));
        assertEquals("decryptedSecret", properties.get("DB_PASS"));
        verify(cryptoService, times(1)).decrypt("{\"kem_ciphertext\":\"...\"}");
    }

    @Test
    void testCreateConfig_WhenEncryptedRequested() {
        NNPConfig newConfig = config("app1", "dev", "latest", "API_KEY", "plainSecret", true);

        when(cryptoService.isEncryptedPayload("plainSecret")).thenReturn(false);
        when(cryptoService.encrypt("plainSecret")).thenReturn("{\"kem_ciphertext\":\"enc\"}");
        when(configRepo.save(any(NNPConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NNPConfig created = configService.createConfig(newConfig);

        assertNotNull(created);
        assertEquals("{\"kem_ciphertext\":\"enc\"}", created.getValue());
        assertTrue(created.getIsEncrypted());
        verify(cryptoService).encrypt("plainSecret");
        verify(configRepo).save(created);
    }

    @Test
    void testCreateConfig_WhenPlaintextRequested() {
        NNPConfig newConfig = config("app1", "dev", "latest", "APP_NAME", "my-service", false);

        when(configRepo.save(any(NNPConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NNPConfig created = configService.createConfig(newConfig);

        assertNotNull(created);
        assertEquals("my-service", created.getValue());
        assertFalse(created.getIsEncrypted());
        verifyNoInteractions(cryptoService);
        verify(configRepo).save(created);
    }

    @Test
    void testUpdateConfig_WhenEncryptedRequested() {
        NNPConfig existing = config("app1", "dev", "latest", "API_KEY", "oldValue", false);
        NNPConfig updateReq = config("app1", "dev", "latest", "API_KEY", "newSecret", true);

        when(configRepo.findById(updateReq.getId())).thenReturn(Optional.of(existing));
        when(cryptoService.isEncryptedPayload("newSecret")).thenReturn(false);
        when(cryptoService.encrypt("newSecret")).thenReturn("{\"kem_ciphertext\":\"newEnc\"}");
        when(configRepo.save(any(NNPConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NNPConfig updated = configService.updateConfig(updateReq);

        assertNotNull(updated);
        assertEquals("{\"kem_ciphertext\":\"newEnc\"}", updated.getValue());
        assertTrue(updated.getIsEncrypted());
        verify(cryptoService).encrypt("newSecret");
        verify(configRepo).save(existing);
    }

    @Test
    void testEncryptApplicationConfigs_SkipsAlreadyEncrypted() {
        NNPConfig alreadyEncrypted = config("app1", "dev", "latest", "KEY1", "{\"kem_ciphertext\":\"k1\"}", true);
        NNPConfig plaintext = config("app1", "dev", "latest", "KEY2", "raw-secret", false);

        List<NNPConfig> configs = List.of(alreadyEncrypted, plaintext);
        when(configRepo.findByApplication("app1")).thenReturn(configs);
        when(cryptoService.encrypt("raw-secret")).thenReturn("{\"kem_ciphertext\":\"k2\"}");

        AppMigrationResultTO result = configService.encryptApplicationConfigs("app1");

        assertNotNull(result);
        assertEquals("app1", result.getApplication());
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getProcessed());
        assertEquals(1, result.getSkipped());
        assertEquals("SUCCESS", result.getStatus());

        verify(cryptoService, times(1)).encrypt("raw-secret");
        verify(configRepo, times(1)).save(plaintext);
        verify(configRepo, never()).save(alreadyEncrypted);
    }

    @Test
    void testDecryptApplicationConfigs_SkipsAlreadyPlaintext() {
        NNPConfig alreadyPlaintext = config("app1", "dev", "latest", "KEY1", "plaintext-val", false);
        NNPConfig encrypted = config("app1", "dev", "latest", "KEY2", "{\"kem_ciphertext\":\"k2\"}", true);

        List<NNPConfig> configs = List.of(alreadyPlaintext, encrypted);
        when(configRepo.findByApplication("app1")).thenReturn(configs);
        when(cryptoService.decrypt("{\"kem_ciphertext\":\"k2\"}")).thenReturn("decrypted-val");

        AppMigrationResultTO result = configService.decryptApplicationConfigs("app1");

        assertNotNull(result);
        assertEquals("app1", result.getApplication());
        assertEquals(2, result.getTotalRecords());
        assertEquals(1, result.getProcessed());
        assertEquals(1, result.getSkipped());
        assertEquals("SUCCESS", result.getStatus());

        verify(cryptoService, times(1)).decrypt("{\"kem_ciphertext\":\"k2\"}");
        verify(configRepo, times(1)).save(encrypted);
        verify(configRepo, never()).save(alreadyPlaintext);
    }

    @Test
    void testBulkCreateOrUpdate() {
        NNPConfig c1 = config("app1", "dev", "latest", "KEY1", "val1", false);
        NNPConfig c2 = config("app1", "dev", "latest", "KEY2", "val2", false);

        when(configRepo.existsById(c1.getId())).thenReturn(true);
        when(configRepo.findById(c1.getId())).thenReturn(Optional.of(c1));
        when(configRepo.existsById(c2.getId())).thenReturn(false);
        when(configRepo.save(any(NNPConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<NNPConfig> results = configService.bulkCreateOrUpdate(List.of(c1, c2));

        assertEquals(2, results.size());
        verify(configRepo, times(2)).save(any(NNPConfig.class));
    }
}
