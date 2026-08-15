package com.commercex.repository;
import com.commercex.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface OrderRepository extends JpaRepository<Order,Long>{
    List<Order> findByUserOrderByCreatedAtDesc(AppUser user);

    /**
     * Loads an order with its line items attached. The detail view and the
     * cancel flow both read items after the transaction closes, and with
     * spring.jpa.open-in-view=false a plain findById leaves them lazy.
     */
    @Query("select distinct o from Order o left join fetch o.items where o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
}
