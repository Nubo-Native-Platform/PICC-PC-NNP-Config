package com.nnp.common.config.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import com.nnp.common.config.service.intf.INNPDomainValServ;
import com.nnp.common.config.to.DomainValueTO;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/domain")
@Slf4j
public class NNPDomainValueController {
	private final INNPDomainValServ domainValSrv;
	public NNPDomainValueController(INNPDomainValServ domainValSrv) {
        this.domainValSrv = domainValSrv;
    }
	@GetMapping(path = "/domval/{appName}/{domName}")
	public List<String> getDomainValueByAppNameAndDomName(@PathVariable String appName,@PathVariable String domName) {
		return domainValSrv.getDomainValueByAppNameAndDomain(appName, domName);
	}
	
	@GetMapping(path = "/all/domval/{appName}")
	public List<DomainValueTO> getAllDomainValueByAppName(@PathVariable String appName) {
		return domainValSrv.getDomainValueByAppName(appName);
	}
	
	@GetMapping(path = "/all/domval")
	public Map<String, List<String>> getAllDomainValue(@RequestParam List<String> domainName) {
		Map<String, List<String>> domainMap = new HashMap<String, List<String>>();
		domainName.forEach(domain ->
			domainMap.put(domain, domainValSrv.getDomainValueByDomain(domain))
		);
		return domainMap;
	}

	@GetMapping(path = "/all")
	public List<DomainValueTO> getAllDomainValues() {
		return domainValSrv.getAllDomainValues();
	}

	@PostMapping()
	public DomainValueTO createDomainValue(@Valid @RequestBody DomainValueTO to) {
		return domainValSrv.createDomainValue(to);
	}

	@PutMapping()
	public DomainValueTO updateDomainValue(@Valid @RequestBody DomainValueTO to) {
		return domainValSrv.updateDomainValue(to);
	}

	@DeleteMapping(path = "/{id}")
	public boolean deleteDomainValue(@NotBlank @PathVariable String id) {
		return domainValSrv.deleteDomainValue(id);
	}

}
