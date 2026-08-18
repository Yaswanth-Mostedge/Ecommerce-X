package com.commercex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.commercex.model.Banner;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
}
