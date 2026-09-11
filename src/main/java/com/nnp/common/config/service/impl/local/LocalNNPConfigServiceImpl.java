package com.nnp.common.config.service.impl.local;

import static com.nnp.common.config.constants.INNPConfigServerConstants.APPLICATION_COMMON;
import static com.nnp.common.config.constants.INNPConfigServerConstants.CLASSPATH_PREFIX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nnp.common.config.entity.NNPConfig;
import com.nnp.common.config.entity.NNPConfigPK;
import com.nnp.common.config.utils.LogUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.nnp.common.config.service.impl.local.LocalConfigTO.Application;
import com.nnp.common.config.service.impl.local.LocalConfigTO.Label;
import com.nnp.common.config.service.intf.INNPConfigService;

import lombok.extern.slf4j.Slf4j;

/**
 * This service class will be loaded if profile local is active, if loaded it
 * will fetch configuration properties from the local store, i.e. the
 * application-local.yml and classpath:local.
 * 
 * Along with the given application name, this service will also fetch a default
 * application named 'common'
 
 */
@Service("localNNPConfigServiceImpl")
@Slf4j
@Profile("local")
public class LocalNNPConfigServiceImpl implements INNPConfigService {
	private final LocalConfigTO configTo;
    public LocalNNPConfigServiceImpl(LocalConfigTO configTo) {
        this.configTo = configTo;
    }
	@Override
	public Map<String, String> getConfigProperties(String application, String profile, String tag) {

		Map<String, String> properties = new HashMap<String, String>();
		
		properties.putAll(_getConfigProperties(APPLICATION_COMMON, profile, tag));
		properties.putAll(_getConfigProperties(application, profile, tag));
		
		return properties;
	}

	private Map<String, String> _getConfigProperties(String application, String profile, String tag) {

		Map<String, String> properties = new HashMap<String, String>();

		Application appTo = configTo.getApplicationMap().get(application);
		if (appTo != null) {
			com.nnp.common.config.service.impl.local.LocalConfigTO.Profile profileTo = appTo.getProfileMap()
					.get(profile);
			if (profileTo != null && tag.equals(profileTo.getLabel().getName())) {
				Label label = profileTo.getLabel();
				Map<String, String> labelProperties = label.getProperties();
				labelProperties.forEach((K, V) -> {
					if (V.startsWith(CLASSPATH_PREFIX)) {
						properties.put(K, label.getFileContentMap().get(K));
					} else {
						properties.put(K, V);
					}
				});
			} else {
				log.error("Configuration could not be found for Profile {} or label {}",  LogUtils.sanitizeForLog(profile), LogUtils.sanitizeForLog(tag));
			}
		} else {
			log.error("No application could have been found with name {}", LogUtils.sanitizeForLog(application));
		}

		return properties;
	}

	@Override
	public List<NNPConfig> getAllConfig() {
		return List.of();
	}

	@Override
	public  NNPConfig createConfig(NNPConfig config) {
		return config;
	}

	@Override
	public NNPConfig updateConfig(NNPConfig config) {
		return config;
	}

	@Override
	public boolean deleteConfig(NNPConfigPK configPK) {
		return true;
	}

	@Override
	public com.nnp.common.config.to.AppMigrationResultTO encryptApplicationConfigs(String application) {
		return com.nnp.common.config.to.AppMigrationResultTO.builder()
				.application(application)
				.status("SKIPPED")
				.message("Local configuration store does not support application encryption migration")
				.build();
	}

	@Override
	public com.nnp.common.config.to.AppMigrationResultTO decryptApplicationConfigs(String application) {
		return com.nnp.common.config.to.AppMigrationResultTO.builder()
				.application(application)
				.status("SKIPPED")
				.message("Local configuration store does not support application decryption migration")
				.build();
	}

	@Override
	public List<NNPConfig> bulkCreateOrUpdate(List<NNPConfig> configs) {
		return configs != null ? configs : List.of();
	}

}

