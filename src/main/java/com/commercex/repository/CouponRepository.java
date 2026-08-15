package com.commercex.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commercex.model.Coupon;

@Repository
public interface CouponRepository
        extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCodeIgnoreCase(String code);

    Optional<Coupon> findByCodeIgnoreCaseAndActiveTrue(
            String code
    );

    boolean existsByCodeIgnoreCase(String code);
}