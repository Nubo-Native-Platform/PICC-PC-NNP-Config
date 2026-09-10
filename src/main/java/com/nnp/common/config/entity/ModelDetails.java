package com.nnp.common.config.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Table(name = "model_details", schema = "\"nnp-rag\"")
@Data
@Builder
public class ModelDetails implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "\"ModelName\"")
    private String modelName;

    @Column(name = "\"API_Key\"")
    private String apiKey;

    @Column(name = "\"Status\"")
    private String status;

    @Column(name = "\"Model_Type\"")
    private String modelType;

    @Column(name = "\"Usage\"")
    private String usage;
}
