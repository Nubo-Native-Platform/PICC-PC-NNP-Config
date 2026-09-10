package com.nnp.common.config.repo;

import com.nnp.common.config.entity.ModelDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NNPModelDetailsRepo extends JpaRepository<ModelDetails, String> {

    @Query(value = "SELECT \"ModelName\" AS \"model_name\", \"API_Key\" AS \"api_key\", \"Status\" AS \"status\", \"Model_Type\" AS \"model_type\", \"Usage\" AS \"usage\" FROM \"nnp-rag\".model_details", nativeQuery = true)
    List<ModelDetails> findAllModels();

    @Query(value = "SELECT \"ModelName\" AS \"model_name\", \"API_Key\" AS \"api_key\", \"Status\" AS \"status\", \"Model_Type\" AS \"model_type\", \"Usage\" AS \"usage\" FROM \"nnp-rag\".model_details WHERE \"ModelName\" = :modelName", nativeQuery = true)
    Optional<ModelDetails> findModelByName(@Param("modelName") String modelName);

    @Query(value = "SELECT COUNT(*) > 0 FROM \"nnp-rag\".model_details WHERE \"ModelName\" = :modelName", nativeQuery = true)
    boolean existsModelByName(@Param("modelName") String modelName);

    @Modifying
    @Query(value = "INSERT INTO \"nnp-rag\".model_details (\"ModelName\", \"API_Key\", \"Status\", \"Model_Type\", \"Usage\") VALUES (:modelName, :apiKey, :status, :modelType, :usage)", nativeQuery = true)
    void insertModel(@Param("modelName") String modelName, @Param("apiKey") String apiKey, @Param("status") String status, @Param("modelType") String modelType, @Param("usage") String usage);

    @Modifying
    @Query(value = "UPDATE \"nnp-rag\".model_details SET \"API_Key\" = :apiKey, \"Status\" = :status, \"Model_Type\" = :modelType, \"Usage\" = :usage WHERE \"ModelName\" = :modelName", nativeQuery = true)
    int updateModelDetails(@Param("modelName") String modelName, @Param("apiKey") String apiKey, @Param("status") String status, @Param("modelType") String modelType, @Param("usage") String usage);

    @Modifying
    @Query(value = "DELETE FROM \"nnp-rag\".model_details WHERE \"ModelName\" = :modelName", nativeQuery = true)
    void deleteModelByName(@Param("modelName") String modelName);
}
