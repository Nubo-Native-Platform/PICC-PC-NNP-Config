package com.nnp.common.config.service.intf;

import com.nnp.common.config.entity.ModelDetails;
import com.nnp.common.config.to.ModelDetailsTO;

import java.util.List;

public interface IModelDetailsService {
    List<ModelDetails> getAllModels();
    ModelDetailsTO createModel(ModelDetailsTO model);
    ModelDetailsTO updateModel(ModelDetailsTO model);
    boolean deleteModel(String modelName);
}
