package com.nnp.common.config.service.intf;

import com.nnp.common.config.entity.NNPConfig;
import com.nnp.common.config.entity.NNPConfigPK;
import com.nnp.common.config.to.AppMigrationResultTO;

import java.util.List;
import java.util.Map;

public interface INNPConfigService {

	/**
	 * 
	 * @param application
	 * @param profile
	 * @param tag
	 * @return Map<String, String>
	 */
	Map<String, String> getConfigProperties(String application, String profile, String tag);

	List<NNPConfig> getAllConfig();

	NNPConfig createConfig(NNPConfig config);

	NNPConfig updateConfig(NNPConfig config);

	boolean deleteConfig(NNPConfigPK key);

	AppMigrationResultTO encryptApplicationConfigs(String application);

	AppMigrationResultTO decryptApplicationConfigs(String application);

	List<NNPConfig> bulkCreateOrUpdate(List<NNPConfig> configs);
}
