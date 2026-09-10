package com.nnp.common.config.entity;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Timestamp;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "nnp_config", schema = "configsrv")
@NamedQuery(name = "NNPConfig.findConfigForGivenAppAndCommon", query = "SELECT C FROM NNPConfig C WHERE (C.id.application = ?1 OR C.id.application = 'common') AND C.id.profile = ?2 AND C.id.tag = ?3")
@Data
public class NNPConfig implements Serializable {

	@Serial
    private static final long serialVersionUID = 4999730945355308887L;

	@EmbeddedId
	private NNPConfigPK id;
	@CreatedBy
	@Column(name = "created_by", updatable = false)
	private String createdBy;
	@CreatedDate
	@Column(name = "date_created", updatable = false)
	private Timestamp dateCreated;
	@LastModifiedDate
	@Column(name = "date_modified")
	private Timestamp dateModified;
	@LastModifiedBy
	@Column(name = "modified_by")
	private String modifiedBy;

	@Column(name = "value")
	private String value;

	@Column(name = "is_encrypted")
	private Boolean isEncrypted = false;

	public Boolean getIsEncrypted() {
		return isEncrypted != null ? isEncrypted : false;
	}

}