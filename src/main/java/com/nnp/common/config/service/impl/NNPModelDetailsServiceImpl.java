package com.nnp.common.config.service.impl;

import com.nnp.common.config.entity.ModelDetails;
import com.nnp.common.config.repo.NNPModelDetailsRepo;
import com.nnp.common.config.service.intf.IModelDetailsService;
import com.nnp.common.config.to.ModelDetailsTO;
import com.nnp.common.config.utils.LogUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@Transactional
public class NNPModelDetailsServiceImpl implements IModelDetailsService {
    private final NNPModelDetailsRepo nnpModelDetailsRepo;
    public NNPModelDetailsServiceImpl(NNPModelDetailsRepo nnpModelDetailsRepo)
        {
        this.nnpModelDetailsRepo = nnpModelDetailsRepo;
        }
    @Override
    public List<ModelDetails> getAllModels() {
        return nnpModelDetailsRepo.findAllModels();
    }

    @Override
    public ModelDetailsTO createModel(ModelDetailsTO model) {
        nnpModelDetailsRepo.insertModel(
                model.getModelName(),
                model.getApiKey(),
                model.getStatus(),
                model.getModelType(),
                model.getUsage()
        );
        return model;
    }

    @Override
    public ModelDetailsTO updateModel(ModelDetailsTO model) {
        int updatedRows = nnpModelDetailsRepo.updateModelDetails(
                model.getModelName(),
                model.getApiKey(),
                model.getStatus(),
                model.getModelType(),
                model.getUsage()
        );
        if (updatedRows > 0) {
            return model;
        } else {
            log.error("Model with name {} not found for update", LogUtils.sanitizeForLog(model.getModelName()));
            return null;
        }
    }

    @Override
    public boolean deleteModel(String modelName) {
        boolean exists = nnpModelDetailsRepo.existsModelByName(modelName);
        if (!exists) {
            log.error("Model with name {} does not exist", LogUtils.sanitizeForLog(modelName));
            return false;
        }
        nnpModelDetailsRepo.deleteModelByName(modelName);
        return true;
    }
}
