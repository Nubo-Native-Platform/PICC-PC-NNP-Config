package com.nnp.common.config.repo;

import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.nnp.common.config.entity.NNPConfig;
import com.nnp.common.config.entity.NNPConfigPK;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
@Profile("jdbc")
public interface NNPConfigRepo extends CrudRepository<NNPConfig, NNPConfigPK> {

	List<NNPConfig> findAll(Example<NNPConfig> config);

	List<NNPConfig> findConfigForGivenAppAndCommon(String application, String profile, String tag);

	@Query("SELECT C FROM NNPConfig C WHERE C.id.application = :application")
	List<NNPConfig> findByApplication(@Param("application") String application);

	List<NNPConfig> findByIdApplication(String application);
}

