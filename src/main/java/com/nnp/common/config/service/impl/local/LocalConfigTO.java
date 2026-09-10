package com.nnp.common.config.service.impl.local;

import static com.nnp.common.config.constants.INNPConfigServerConstants.CLASSPATH_PREFIX;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nnp.common.config.utils.LogUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This TO class represents a data structure to store the local configuration.
 * This is active only for local profile.
 
 */
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties("localconfig")
@Profile("local")
@Data
@Slf4j
public class LocalConfigTO {

	private List<Application> application;
	private Map<String, Application> applicationMap;

	@PostConstruct
	public void constructApplicationMap() {

		applicationMap = new HashMap<String, LocalConfigTO.Application>();
		application.forEach(A -> {
			A.constructProfileMap();
			applicationMap.put(A.getName(), A);
		});

	}

	@Data
	public static class Label {

		private String name;
		private Map<String, String> properties;
		private Map<String, String> fileContentMap;

		private ResourceLoader resourceLoader = new DefaultResourceLoader();

		public void constructFileContentMap() {
			fileContentMap = new HashMap<String, String>();
			properties.forEach((K, V) -> {
				if (V.startsWith(CLASSPATH_PREFIX)) {
					Resource resource = resourceLoader.getResource(V);
					try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
						String fileContentAsString = FileCopyUtils.copyToString(reader);
						fileContentMap.put(K, fileContentAsString);
					} catch (IOException e) {
						log.error(LogUtils.sanitizeForLog(e.getMessage()),LogUtils.sanitizeForLog( e.toString()));
					}
				}
			});
		}
	}

	@Data
	public static class Profile {

		private String name;
		private Label label;
	}

	@Data
	public static class Application {

		private String name;
		private List<Profile> profiles;
		private Map<String, Profile> profileMap;

		public void constructProfileMap() {
			profileMap = new HashMap<String, LocalConfigTO.Profile>();
			profiles.forEach(P -> {
				P.getLabel().constructFileContentMap();
				profileMap.put(P.getName(), P);
			});
		}
	}

}
