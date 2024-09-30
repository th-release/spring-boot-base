package com.threlease.base.repositories.auth;

import com.threlease.base.entites.AuthEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<AuthEntity, String>, UserCustomRepository {
    @Query("SELECT u FROM AuthEntity u WHERE u.uuid = :uuid")
    Optional<AuthEntity> findOneByUUID(@Param("uuid") String uuid);

    @Query(value = "SELECT u FROM AuthEntity u")
    Page<AuthEntity> findByPagination(Pageable pageable);
}
