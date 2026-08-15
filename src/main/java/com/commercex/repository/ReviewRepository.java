package com.commercex.repository;
import com.commercex.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ReviewRepository extends JpaRepository<Review,Long>{
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
}
