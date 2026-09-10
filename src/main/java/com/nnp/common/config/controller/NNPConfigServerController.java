package com.nnp.common.config.controller;

import com.nnp.common.config.to.AppMigrationResultTO;
import com.nnp.common.config.to.NNPConfigResponseTo;
import com.nnp.common.config.utils.LogUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.config.environment.EnvironmentMediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nnp.common.config.service.impl.NNPConfigOrchestratorService;
import com.nnp.common.config.to.ConfigResponseTO;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping(path = "nnp-config")
@Slf4j
@Tag(name = "NNP Configuration", description = "Endpoints for managing configuration properties, post-quantum encryption, and application migration")
public class NNPConfigServerController {

	private final NNPConfigOrchestratorService cfgOrchSvc;

	public NNPConfigServerController(NNPConfigOrchestratorService cfgOrchSvc) {
		this.cfgOrchSvc = cfgOrchSvc;
	}

	/**
	 * 
	 * @param application
	 * @param profiles
	 * @param tag
	 * @return ResponseEntity<ConfigResponseTO>
	 * @throws IllegalArgumentException
	 */
	@Operation(summary = "Get resolved configuration for an application", description = "Retrieves configuration for the given application, profiles, and tag. Encrypted values are automatically decrypted.")
	@RequestMapping(value = "{application}/{profiles}/{tag}", method = RequestMethod.GET, produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<ConfigResponseTO> getValueOnly(@PathVariable("application") final String application,
			@PathVariable("profiles") final String profiles, @PathVariable("tag") final String tag) {

		if (application == null || profiles == null || tag == null) {
			log.error("application or profiles or tag can't be null");
			throw new IllegalArgumentException("application or profiles or tag can't be null");
		}

		ConfigResponseTO configResponseTO = cfgOrchSvc.getConfigResponse(application, profiles, tag);
        return new ResponseEntity<ConfigResponseTO>(configResponseTO,
				HttpStatus.OK);
	}

	@Operation(summary = "Get all configuration properties", description = "Retrieves all configuration records with their encryption status.")
	@GetMapping(produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<List<NNPConfigResponseTo>> getAllConfig() {
		List<NNPConfigResponseTo> configResponseList = cfgOrchSvc.getAllConfig();
        return new ResponseEntity<>(configResponseList, HttpStatus.OK);
	}

	@Operation(summary = "Create configuration property", description = "Creates a new configuration. If is_encrypted=true, encrypts the value via the quantum-safe service.")
	@PostMapping(produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<NNPConfigResponseTo> createConfig(@Valid @RequestBody NNPConfigResponseTo config) {
		NNPConfigResponseTo createdConfig = cfgOrchSvc.createConfig(config);
        return new ResponseEntity<>(createdConfig, HttpStatus.CREATED);
	}

	@Operation(summary = "Update configuration property", description = "Updates an existing configuration. If is_encrypted=true, encrypts the new value.")
	@PutMapping(produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<NNPConfigResponseTo> updateConfig(@Valid @RequestBody NNPConfigResponseTo config) {
		NNPConfigResponseTo updatedConfig = cfgOrchSvc.updateConfig(config);
        return new ResponseEntity<>(updatedConfig, HttpStatus.OK);
	}

	@Operation(summary = "Delete configuration property", description = "Deletes a configuration property by composite key.")
	@DeleteMapping()
	public ResponseEntity<Void> deleteConfig(@Valid @RequestBody NNPConfigResponseTo config) {
		boolean deleted = cfgOrchSvc.deleteConfig(config);
		return new ResponseEntity<>(deleted ? HttpStatus.OK : HttpStatus.NOT_FOUND);
	}

	@Operation(summary = "Encrypt all configurations for an application", description = "Finds all records for the given application and encrypts any plaintext values using the quantum-safe service.")
	@PostMapping(value = "{application}/encrypt", produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<AppMigrationResultTO> encryptAppConfigs(@PathVariable("application") final String application) {
		log.info("Request received to encrypt all configuration values for application: {}", LogUtils.sanitizeForLog(application));
		AppMigrationResultTO result = cfgOrchSvc.encryptApplicationConfigs(application);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Encrypt all configurations for an application (alias)", description = "Alternate route to encrypt all configuration values for an application.")
	@PostMapping(value = "encrypt/{application}", produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<AppMigrationResultTO> encryptAppConfigsAlt(@PathVariable("application") final String application) {
		return encryptAppConfigs(application);
	}

	@Operation(summary = "Decrypt all configurations for an application", description = "Finds all records for the given application and decrypts any encrypted values using the quantum-safe service.")
	@PostMapping(value = "{application}/decrypt", produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<AppMigrationResultTO> decryptAppConfigs(@PathVariable("application") final String application) {
		log.info("Request received to decrypt all configuration values for application: {}", LogUtils.sanitizeForLog(application));
		AppMigrationResultTO result = cfgOrchSvc.decryptApplicationConfigs(application);
		return new ResponseEntity<>(result, HttpStatus.OK);
	}

	@Operation(summary = "Decrypt all configurations for an application (alias)", description = "Alternate route to decrypt all configuration values for an application.")
	@PostMapping(value = "decrypt/{application}", produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<AppMigrationResultTO> decryptAppConfigsAlt(@PathVariable("application") final String application) {
		return decryptAppConfigs(application);
	}

	@Operation(summary = "Bulk create or update configurations", description = "Accepts a list of configuration objects to create or update in bulk with encryption handling.")
	@PostMapping(value = "bulk", produces = EnvironmentMediaType.V2_JSON)
	public ResponseEntity<List<NNPConfigResponseTo>> bulkConfig(@Valid @RequestBody List<NNPConfigResponseTo> configs) {
		log.info("Request received for bulk configuration save/update for {} items", LogUtils.sanitizeForLog(String.valueOf(configs != null ? configs.size() : 0)));
		List<NNPConfigResponseTo> saved = cfgOrchSvc.bulkCreateOrUpdate(configs);
		return new ResponseEntity<>(saved, HttpStatus.OK);
	}

}
