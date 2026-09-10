package com.nnp.common.config.service.intf;

import java.util.List;

import com.nnp.common.config.to.DomainValueTO;

public interface INNPDomainValServ {

	List<String> getDomainValueByAppNameAndDomain(String appName, String domainName);
	List<DomainValueTO> getDomainValueByAppName(String appName);
	List<String> getDomainValueByDomain(String domain);
	List<DomainValueTO> getAllDomainValues();
	DomainValueTO createDomainValue(DomainValueTO to);
	DomainValueTO updateDomainValue(DomainValueTO to);
	boolean deleteDomainValue(String id);
}
