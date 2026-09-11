package com.nnp.common.config.controller;

import com.nnp.common.config.entity.ModelDetails;
import com.nnp.common.config.service.intf.IModelDetailsService;
import com.nnp.common.config.to.ModelDetailsTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "nnp-config/api/v1/model-details")
@Slf4j
public class NNPModelDetailsController {

    private final IModelDetailsService imodelDetailsService;

    public NNPModelDetailsController(IModelDetailsService modelDetailsService) {
        this.imodelDetailsService = modelDetailsService;
    }

    @GetMapping()
    public ResponseEntity<List<ModelDetails>> getAllModels() {
        List<ModelDetails> models = imodelDetailsService.getAllModels();
        return new ResponseEntity<>(models, HttpStatus.OK);
    }

    @PostMapping()
    public ResponseEntity<ModelDetailsTO> createModel(@Valid @RequestBody ModelDetailsTO model) {
        ModelDetailsTO createdModel = imodelDetailsService.createModel(model);
        return new ResponseEntity<>(createdModel, HttpStatus.CREATED);
    }

    @PutMapping()
    public ResponseEntity<ModelDetailsTO> updateModel(@Valid @RequestBody ModelDetailsTO model) {
        ModelDetailsTO updatedModel = imodelDetailsService.updateModel(model);
        return updatedModel != null ? new ResponseEntity<>(updatedModel, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping(path = "{modelName}")
    public ResponseEntity<Void> deleteModel(@PathVariable String modelName) {
        boolean deleted = imodelDetailsService.deleteModel(modelName);
        return new ResponseEntity<>(deleted ? HttpStatus.OK : HttpStatus.NOT_FOUND);
    }
}
