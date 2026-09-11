package com.nnp.common.config.entity;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "nnp_domain_value",schema = "configsrv")
@Getter
@Setter
public class NNPDomainValue implements Serializable{
	@Serial
    private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dv_id")
	private String dvId;
	
	@Column(name = "ui_application")
	private String uiAppName;
	
	@Column(name = "domain_name")
	private String domainName;
	
	@Column(name = "domain_value")
	private String domainVal;
	
	@Column(name = "date_created")
	private LocalDateTime createdOn;

}
