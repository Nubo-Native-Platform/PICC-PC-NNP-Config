package com.nnp.common.config.to;

import static com.nnp.common.config.constants.INNPConfigServerConstants.HYPHEN;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;


@Data
public class ConfigPropertiesTO implements Serializable {

	@Serial
    private static final long serialVersionUID = -5203698598747822854L;

	private String name;
	private Map<String, String> source = new HashMap<String, String>();

	public void addValue(String key, String value) {
		source.put(key, value);
	}

	public void addName(String app, String profile) {
		name = app + HYPHEN + profile;
	}

}
