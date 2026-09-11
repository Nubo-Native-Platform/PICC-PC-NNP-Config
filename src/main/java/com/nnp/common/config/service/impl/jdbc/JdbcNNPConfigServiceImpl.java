package com.nnp.common.config.service.impl.jdbc;

import static com.nnp.common.config.constants.INNPConfigServerConstants.APPLICATION_COMMON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nnp.common.config.crypto.QuantumSafeCryptoService;
import com.nnp.common.config.to.AppMigrationResultTO;
import com.nnp.common.config.utils.LogUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nnp.common.config.entity.NNPConfig;
import com.nnp.common.config.entity.NNPConfigPK;
import com.nnp.common.config.repo.NNPConfigRepo;
import com.nnp.common.config.service.intf.INNPConfigService;

import lombok.extern.slf4j.Slf4j;

/**
 * This service class will be loaded if profile jdbc is active, if loaded it
 * will fetch configuration properties from the jdbc backend.
 * 
 * Along with the given application name, this service will also fetch a default
 * application named 'common'
 * 
 */
@Transactional
@Service("jdbcNNPConfigServiceImpl")
@Slf4j
@Profile("jdbc")
public class JdbcNNPConfigServiceImpl implements INNPConfigService {
	private final NNPConfigRepo configRepo;
	private final QuantumSafeCryptoService cryptoService;
    public JdbcNNPConfigServiceImpl(NNPConfigRepo configRepo, QuantumSafeCryptoService cryptoService) {
        this.configRepo = configRepo;
        this.cryptoService = cryptoService;
    }
	@Override
	public Map<String, String> getConfigProperties(String application, String profile, String tag) {

		Map<String, String> allProperties = new HashMap<String, String>();
		Map<String, String> appSpecificProperties = new HashMap<String, String>();

		log.debug("JPA query is being triggered for application >> {} and {}, profile >> {}, tag >> {}", LogUtils.sanitizeForLog(application),
				LogUtils.sanitizeForLog(APPLICATION_COMMON), LogUtils.sanitizeForLog(profile), LogUtils.sanitizeForLog(tag));

		List<NNPConfig> configEntityList = configRepo.findConfigForGivenAppAndCommon(application, profile, tag);
		log.debug("JPA Query has returned");
		if (configEntityList.isEmpty()) {
			log.warn("Configuration could not be found for Profile {} or label {}", LogUtils.sanitizeForLog(profile), LogUtils.sanitizeForLog(tag));
		} else {
			configEntityList.forEach(X -> {
				NNPConfigPK id = X.getId();
				String resolvedValue = X.getValue();
				if (Boolean.TRUE.equals(X.getIsEncrypted()) && resolvedValue != null) {
					log.debug("Decrypting configuration key: {}", LogUtils.sanitizeForLog(id.getKey()));
					resolvedValue = cryptoService.decrypt(resolvedValue);
				}
				if (APPLICATION_COMMON.equals(id.getApplication())) {
					allProperties.put(id.getKey(), resolvedValue);
				} else {
					appSpecificProperties.put(id.getKey(), resolvedValue);
				}
			});
			allProperties.putAll(appSpecificProperties);// For overriding common properties which has same key
		}

		return allProperties;
	}

	@Override
	public List<NNPConfig> getAllConfig() {
		log.debug("query being triggered to fetch all configurations");
		return (List<NNPConfig>) configRepo.findAll();
	}

	@Override
	public NNPConfig createConfig(NNPConfig config) {
		log.debug("Creating new configuration with key: {}", LogUtils.sanitizeForLog(config.getId().getKey()));
		if (Boolean.TRUE.equals(config.getIsEncrypted())) {
			if (config.getValue() != null && !cryptoService.isEncryptedPayload(config.getValue())) {
				String encryptedVal = cryptoService.encrypt(config.getValue());
				config.setValue(encryptedVal);
			}
			config.setIsEncrypted(true);
		} else {
			config.setIsEncrypted(false);
		}
		return configRepo.save(config);
	}

	@Override
	public NNPConfig updateConfig(NNPConfig config) {
		log.debug("Updating configuration with key: {}",LogUtils.sanitizeForLog( config.getId().getKey()));
		NNPConfig existingConfig = configRepo.findById(config.getId()).orElse(null);
		if (existingConfig != null) {
			if (Boolean.TRUE.equals(config.getIsEncrypted())) {
				if (config.getValue() != null && !cryptoService.isEncryptedPayload(config.getValue())) {
					String encryptedVal = cryptoService.encrypt(config.getValue());
					existingConfig.setValue(encryptedVal);
				} else {
					existingConfig.setValue(config.getValue());
				}
				existingConfig.setIsEncrypted(true);
			} else {
				existingConfig.setValue(config.getValue());
				existingConfig.setIsEncrypted(false);
			}
			return configRepo.save(existingConfig);
		} else {
			log.warn("Configuration with key {} not found for update",LogUtils.sanitizeForLog( config.getId().getKey()));
			return null;
		}
	}

	@Override
	public boolean deleteConfig(NNPConfigPK configPK) {
		log.debug("Deleting configuration with property: {}", LogUtils.sanitizeForLog(String.valueOf(configPK)));
		boolean exists = configRepo.existsById(configPK);
		if (!exists) {
			log.warn("Configuration with property: {} does not exist", LogUtils.sanitizeForLog(String.valueOf(configPK)));
			return false;
		}
		configRepo.deleteById(configPK);
		log.debug("Configuration with property: {} deleted successfully", LogUtils.sanitizeForLog(String.valueOf(configPK)));
		return true;
	}

	@Override
	public AppMigrationResultTO encryptApplicationConfigs(String application) {
		log.info("Starting encryption migration for application: {}", LogUtils.sanitizeForLog(application));
		List<NNPConfig> configs = configRepo.findByApplication(application);
		int total = configs.size();
		int processed = 0;
		int skipped = 0;

		for (NNPConfig cfg : configs) {
			if (Boolean.TRUE.equals(cfg.getIsEncrypted())) {
				log.debug("Skipping already encrypted configuration: {}", LogUtils.sanitizeForLog(cfg.getId().getKey()));
				skipped++;
				continue;
			}
			if (cfg.getValue() != null) {
				String encryptedValue = cryptoService.encrypt(cfg.getValue());
				cfg.setValue(encryptedValue);
			}
			cfg.setIsEncrypted(true);
			configRepo.save(cfg);
			processed++;
		}

		log.info("Completed encryption migration for application: {}. Total: {}, Processed: {}, Skipped: {}",
				LogUtils.sanitizeForLog(application), LogUtils.sanitizeForLog(String.valueOf(total)), LogUtils.sanitizeForLog(String.valueOf(processed)), LogUtils.sanitizeForLog(String.valueOf(skipped)));

		return AppMigrationResultTO.builder()
				.application(application)
				.totalRecords(total)
				.processed(processed)
				.skipped(skipped)
				.status("SUCCESS")
				.message(String.format("Successfully encrypted %d configuration(s), skipped %d already encrypted", processed, skipped))
				.build();
	}

	@Override
	public AppMigrationResultTO decryptApplicationConfigs(String application) {
		log.info("Starting decryption migration for application: {}", LogUtils.sanitizeForLog(application));
		List<NNPConfig> configs = configRepo.findByApplication(application);
		int total = configs.size();
		int processed = 0;
		int skipped = 0;

		for (NNPConfig cfg : configs) {
			if (!Boolean.TRUE.equals(cfg.getIsEncrypted())) {
				log.debug("Skipping already plaintext configuration: {}",LogUtils.sanitizeForLog( cfg.getId().getKey()));
				skipped++;
				continue;
			}
			if (cfg.getValue() != null) {
				String decryptedValue = cryptoService.decrypt(cfg.getValue());
				cfg.setValue(decryptedValue);
			}
			cfg.setIsEncrypted(false);
			configRepo.save(cfg);
			processed++;
		}

		log.info("Completed decryption migration for application: {}. Total: {}, Processed: {}, Skipped: {}",
				LogUtils.sanitizeForLog(application), LogUtils.sanitizeForLog(String.valueOf(total)), LogUtils.sanitizeForLog(String.valueOf(processed)), LogUtils.sanitizeForLog(String.valueOf(skipped)));

		return AppMigrationResultTO.builder()
				.application(application)
				.totalRecords(total)
				.processed(processed)
				.skipped(skipped)
				.status("SUCCESS")
				.message(String.format("Successfully decrypted %d configuration(s), skipped %d already plaintext", processed, skipped))
				.build();
	}

	@Override
	public List<NNPConfig> bulkCreateOrUpdate(List<NNPConfig> configs) {
		log.info("Bulk creating/updating {} configurations", LogUtils.sanitizeForLog(String.valueOf( configs != null ? configs.size() : 0)));
		List<NNPConfig> result = new ArrayList<>();
		if (configs == null || configs.isEmpty()) {
			return result;
		}

		for (NNPConfig cfg : configs) {
			if (cfg.getId() != null && configRepo.existsById(cfg.getId())) {
				result.add(updateConfig(cfg));
			} else {
				result.add(createConfig(cfg));
			}
		}
		return result;
	}

}
