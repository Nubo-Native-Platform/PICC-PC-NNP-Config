package com.nnp.common.config.to;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nnp.common.config.entity.NNPConfig;
import com.nnp.common.config.entity.NNPConfigPK;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Data
@Getter
@Setter
@NoArgsConstructor
public class NNPConfigResponseTo implements Serializable {
    @Serial
    private static final long serialVersionUID = 3463513618707230574L;

    private String application;
    private String profile;
    private String tag;
    private String key;
    private String value;

    @JsonProperty("is_encrypted")
    @JsonAlias({"isEncrypted", "encrypted"})
    private Boolean isEncrypted = false;

    public NNPConfigResponseTo(String application, String profile, String tag, String key, String value) {
        this(application, profile, tag, key, value, false);
    }

    public NNPConfigResponseTo(String application, String profile, String tag, String key, String value, Boolean isEncrypted) {
        this.application = application;
        this.profile = profile;
        this.tag = tag;
        this.key = key;
        this.value = value;
        this.isEncrypted = isEncrypted != null ? isEncrypted : false;
    }

    public Boolean getIsEncrypted() {
        return isEncrypted != null ? isEncrypted : false;
    }

    public static NNPConfig getEntityModelFromDto(NNPConfigResponseTo dto) {
        NNPConfigPK pk = new NNPConfigPK();
        pk.setApplication(dto.getApplication());
        pk.setProfile(dto.getProfile());
        pk.setTag(dto.getTag());
        pk.setKey(dto.getKey());
        NNPConfig entity = new NNPConfig();
        entity.setId(pk);
        entity.setValue(dto.getValue());
        entity.setIsEncrypted(dto.getIsEncrypted() != null ? dto.getIsEncrypted() : false);
        return entity;
    }

    public static NNPConfigResponseTo getDtoFromEntityModel(NNPConfig entity) {
        NNPConfigResponseTo dto = new NNPConfigResponseTo(
                entity.getId().getApplication(),
                entity.getId().getProfile(),
                entity.getId().getTag(),
                entity.getId().getKey(),
                entity.getValue(),
                entity.getIsEncrypted()
        );
        return dto;
    }
}
