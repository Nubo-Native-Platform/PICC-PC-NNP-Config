package com.nnp.common.config.to;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;


@Data
public class ConfigResponseTO implements Serializable {

	@Serial
    private static final long serialVersionUID = 3463513618707230574L;

	private String name;
	private List<String> profiles;
	private String label;
	private String version;
	private String state;
	private List<ConfigPropertiesTO> propertySources;

	public void addProperties(ConfigPropertiesTO p) {
		if (propertySources == null) {
			propertySources = new ArrayList<ConfigPropertiesTO>();
		}
		propertySources.add(p);
	}

	public void addProfile(String profile) {
		if (profiles == null) {
			profiles = new ArrayList<String>();
		}
		profiles.add(profile);
	}

}
