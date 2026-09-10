package com.nnp.common.config.entity;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class NNPConfigPK implements Serializable {

	@Serial
    private static final long serialVersionUID = 2739231969426914380L;

	@Column(name = "application")
	private String application;

	@Column(name = "profile")
	private String profile;

	@Column(name = "tag")
	private String tag;

	@Column(name = "key")
	private String key;

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		NNPConfigPK that = (NNPConfigPK) o;
		return Objects.equals(application, that.application) &&
				Objects.equals(profile, that.profile) &&
				Objects.equals(tag, that.tag) &&
				Objects.equals(key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(application, profile, tag, key);
	}

}