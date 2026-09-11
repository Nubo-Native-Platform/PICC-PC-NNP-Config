package com.nnp.common.config.to;

import java.io.Serial;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DomainValueTO implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;

	private String id;

	@NotBlank
	private String domainName;

	private String domainVal;

	@JsonIgnore
	private String uiAppName = "nnp-common";

}
