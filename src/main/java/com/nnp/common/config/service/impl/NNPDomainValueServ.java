package com.nnp.common.config.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.nnp.common.config.exception.ConfigServiceException;
import com.nnp.common.config.utils.LogUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import com.nnp.common.config.entity.NNPDomainValue;
import com.nnp.common.config.repo.NNPDomainValueRepo;
import com.nnp.common.config.service.intf.INNPDomainValServ;
import com.nnp.common.config.to.DomainValueTO;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class NNPDomainValueServ implements INNPDomainValServ {
	
	private final NNPDomainValueRepo domainValRepo;
	private final ModelMapper mapper;

	public NNPDomainValueServ(NNPDomainValueRepo domainValRepo, ModelMapper mapper) {
		this.domainValRepo = domainValRepo;
		this.mapper = mapper;
	}

	@Override
	public List<String> getDomainValueByAppNameAndDomain(String appName, String domainName) {
		List<NNPDomainValue> domainValues = domainValRepo.findByUiAppNameAndDomainName(appName, domainName).orElse(null);
		if(domainValues!=null) {
			return domainValues.stream().map(NNPDomainValue::getDomainVal).toList();
		}else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<DomainValueTO> getDomainValueByAppName(String appName) {
		List<NNPDomainValue> domainValues = domainValRepo.findByUiAppName(appName).orElse(null);
		if (domainValues != null) {
			return domainValues.stream()
					.map(domainVal -> mapper.map(domainVal, DomainValueTO.class))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getDomainValueByDomain(String domain) {
		List<NNPDomainValue> domainValues = domainValRepo.findByDomainName(domain).orElse(new ArrayList<NNPDomainValue>());
		return domainValues.stream().map(NNPDomainValue::getDomainVal).collect(Collectors.toList());
	}

	@Override
	public List<DomainValueTO> getAllDomainValues() {
		List<NNPDomainValue> domainValues = domainValRepo.findAll();
		return domainValues.stream().map(domainVal -> mapper.map(domainVal, DomainValueTO.class)).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public DomainValueTO createDomainValue(DomainValueTO domainValueTO) {
		NNPDomainValue domainValue = mapper.map(domainValueTO, NNPDomainValue.class);
		NNPDomainValue resp = domainValRepo.save(domainValue);
		return mapper.map(resp, DomainValueTO.class);
	}

	@Override
	@Transactional
	public DomainValueTO updateDomainValue(DomainValueTO domainValueTO) {
		if(domainValueTO.getId() == null) {
			log.error("id is mandatory for update domain value");
			throw new ConfigServiceException("id is mandatory for update domain value");
		}
		NNPDomainValue domainValue = domainValRepo.findById(domainValueTO.getId()).orElse(null);
		if(domainValue!=null) {
			domainValue.setDomainName(domainValueTO.getDomainName());
			domainValue.setDomainVal(domainValueTO.getDomainVal());
			domainValue =domainValRepo.save(domainValue);
		} else {
			log.error("domain value not found for id - {}", LogUtils.sanitizeForLog(domainValueTO.getId()));
			throw new ConfigServiceException("Domain value not found for id - " + domainValueTO.getId());
		}
		return mapper.map(domainValue, DomainValueTO.class);
	}

	@Override
	@Transactional
	public boolean deleteDomainValue(String id) {
		if(id == null || id.trim().isEmpty()){
			log.error("id is mandatory for delete domain value");
			throw new ConfigServiceException("id is mandatory for delete domain value");
		}
		if (domainValRepo.existsById(id)) {
			domainValRepo.deleteById(id);
			return true;
		} else {
			log.error("domain value not found for id - {}",LogUtils.sanitizeForLog(id));
			throw new ConfigServiceException("Domain value not found for id - " + id);
		}
	}

}
