package com.ecom.validation.repository;

import com.ecom.validation.entity.Validation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface ValidationRepository extends JpaRepository<Validation, Long> {

    Optional<Validation> findByCode(String code);

    Optional<Validation>findById(Long id);

    void deleteAllByExpireAtBefore(Instant now);

    @Transactional
    void deleteByUserIdAndDeviceIdAndActiveFalse(Long userId, Long deviceId);
}
