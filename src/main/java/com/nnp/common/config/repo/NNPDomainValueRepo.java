package com.nnp.common.config.repo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nnp.common.config.entity.NNPDomainValue;

public interface NNPDomainValueRepo extends JpaRepository<NNPDomainValue, String> {

	Optional<List<NNPDomainValue>> findByUiAppNameAndDomainName(String appName, String domainName);
	Optional<List<NNPDomainValue>> findByUiAppName(String appName);
	Optional<List<NNPDomainValue>> findByDomainName(String domainName);
}
