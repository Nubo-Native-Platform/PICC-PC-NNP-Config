package com.nnp.common.config.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ModelDetailsTO {
    private String modelName;
    private String apiKey;
    private String status;
    private String modelType;
    private String usage;
}
