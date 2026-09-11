package com.nnp.common.config.to;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppMigrationResultTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String application;
    private int totalRecords;
    private int processed;
    private int skipped;
    private String status;
    private String message;
}
