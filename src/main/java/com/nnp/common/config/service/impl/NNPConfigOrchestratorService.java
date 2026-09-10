package com.nnp.common.config.service.impl;

import static com.nnp.common.config.constants.INNPConfigServerConstants.COMMA;
import static com.nnp.common.config.constants.INNPConfigServerConstants.SVC_CLASS_SUFFIX;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.nnp.common.config.entity.NNPConfig;
import com.nnp.common.config.to.NNPConfigResponseTo;
import com.nnp.common.config.utils.LogUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.nnp.common.config.service.intf.INNPConfigService;
import com.nnp.common.config.to.AppMigrationResultTO;
import com.nnp.common.config.to.ConfigPropertiesTO;
import com.nnp.common.config.to.ConfigResponseTO;

import lombok.extern.slf4j.Slf4j;

/**
 * For every active profiles (jdbc, local etc.), there will be a configuration
 * store and to retrieve configuration data there will be a configuration
 * service. This class represents an Orchestrator service, which calls such
 * configuration services, merges the returned configuration properties per
 * client name and client profile combination, creates a Response TO and
 * returns.
 * 
 */
@Service
@Slf4j
public class NNPConfigOrchestratorService {

	@Value("${spring.profiles.active}")
	private String cfgServerActiveProfiles;
	private final ApplicationContext appContext;
    public NNPConfigOrchestratorService(ApplicationContext appContext) {
        this.appContext = appContext;
    }
	/**
	 * 
	 * @param application
	 * @param profiles
	 * @param tag
	 * @return ConfigResponseTO
	 */
	public ConfigResponseTO getConfigResponse(String application, String profiles, String tag) {

		ConfigResponseTO cfgRespTo = new ConfigResponseTO();
		cfgRespTo.setName(application);
		cfgRespTo.setLabel(tag);
		String[] clientProfileArr = profiles.split(COMMA);
		for (String clientProfile : clientProfileArr) {
			cfgRespTo.addProfile(clientProfile);
		}

		String[] cfgServerProfileArr = cfgServerActiveProfiles.split(COMMA);

        for (String P : cfgRespTo.getProfiles()) {
            ConfigPropertiesTO cfgPropsTo = new ConfigPropertiesTO();
            cfgPropsTo.addName(application, P);

            for (String profile : cfgServerProfileArr) {

                String serviceClass = profile + SVC_CLASS_SUFFIX;
                Object configSvcObj = appContext.getBean(serviceClass);
                if (configSvcObj instanceof INNPConfigService) {

                    Map<String, String> properties = ((INNPConfigService) configSvcObj)
                            .getConfigProperties(application, P, tag);
                    properties.forEach(cfgPropsTo::addValue);
                } else {
                    log.error(
                            "No Service class should have configured with this name {}, change it immediately and do a server restart",
                            LogUtils.sanitizeForLog(serviceClass));
                }

            }
            cfgRespTo.addProperties(cfgPropsTo);
            // log.debug("Config properties are prepared for profile {}", P);
        }

        return cfgRespTo;
	}

	public List<NNPConfigResponseTo> getAllConfig() {

		return Arrays.stream(cfgServerActiveProfiles.split(COMMA))
				.map(p -> appContext.getBean(p + SVC_CLASS_SUFFIX, INNPConfigService.class))
				.flatMap(b -> b.getAllConfig().stream())
				.map(NNPConfigResponseTo::getDtoFromEntityModel)
				.toList();
	}

	public NNPConfigResponseTo createConfig(NNPConfigResponseTo config) {
		return Arrays.stream(cfgServerActiveProfiles.split(COMMA))
				.map(p -> appContext.getBean(p + SVC_CLASS_SUFFIX, INNPConfigService.class))
				.map(b -> NNPConfigResponseTo
						.getDtoFromEntityModel(b.createConfig(NNPConfigResponseTo.getEntityModelFromDto(config))))
				.findFirst().get();
	}

	public NNPConfigResponseTo updateConfig(NNPConfigResponseTo config) {
		return Arrays.stream(cfgServerActiveProfiles.split(COMMA))
				.map(p -> appContext.getBean(p + SVC_CLASS_SUFFIX, INNPConfigService.class))
				.map(b -> NNPConfigResponseTo
						.getDtoFromEntityModel(b.updateConfig(NNPConfigResponseTo.getEntityModelFromDto(config))))
				.findFirst().get();
	}

	public boolean deleteConfig(NNPConfigResponseTo config) {
		NNPConfig nnpConfig = NNPConfigResponseTo.getEntityModelFromDto(config);
		return Arrays.stream(cfgServerActiveProfiles.split(COMMA))
				.map(p -> appContext.getBean(p + SVC_CLASS_SUFFIX, INNPConfigService.class))
				.map(b -> b.deleteConfig(nnpConfig.getId()))
				.reduce((a, b) -> a || b)
				.orElse(false);
	}

	public AppMigrationResultTO encryptApplicationConfigs(String application) {
		return Arrays.stream(cfgServerActiveProfiles.split(COMMA))
				.map(p -> appContext.getBean(p + SVC_CLASS_SUFFIX, INNPConfigService.class))
				.map(b -> b.encryptApplicationConfigs(application))
				.filter(r -> r != null && !"SKIPPED".equalsIgnoreCase(r.getStatus()))
				.findFirst()
				.orElseGet(() -> AppMigrationResultTO.builder()
						.application(application)
						.status("SUCCESS")
						.message("No active store performed encryption")
						.build());
	}

	public AppMigrationResultTO decryptApplicationConfigs(String application) {
		return Arrays.stream(cfgServerActiveProfiles.split(COMMA))
				.map(p -> appContext.getBean(p + SVC_CLASS_SUFFIX, INNPConfigService.class))
				.map(b -> b.decryptApplicationConfigs(application))
				.filter(r -> r != null && !"SKIPPED".equalsIgnoreCase(r.getStatus()))
				.findFirst()
				.orElseGet(() -> AppMigrationResultTO.builder()
						.application(application)
						.status("SUCCESS")
						.message("No active store performed decryption")
						.build());
	}

	public List<NNPConfigResponseTo> bulkCreateOrUpdate(List<NNPConfigResponseTo> configs) {
		if (configs == null || configs.isEmpty()) {
			return List.of();
		}
		List<NNPConfig> entities = configs.stream()
				.map(NNPConfigResponseTo::getEntityModelFromDto)
				.toList();

		return Arrays.stream(cfgServerActiveProfiles.split(COMMA))
				.map(p -> appContext.getBean(p + SVC_CLASS_SUFFIX, INNPConfigService.class))
				.flatMap(b -> b.bulkCreateOrUpdate(entities).stream())
				.map(NNPConfigResponseTo::getDtoFromEntityModel)
				.toList();
	}
}

