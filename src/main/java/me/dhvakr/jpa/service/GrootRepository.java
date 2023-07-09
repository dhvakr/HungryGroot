package me.dhvakr.jpa.service;

import me.dhvakr.jpa.entity.Groots;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface GrootRepository extends JpaRepository<Groots, Long>, JpaSpecificationExecutor<Groots> {

    //~ PREDEFINED SQL QUERY =============================================================================================================

    Groots findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByforgotPasswordKey(String forgotPasswordKey);

    //~ SQL QUERY ========================================================================================================================

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM groots_food_history WHERE count_recorded_date < ?1", nativeQuery = true)
    void deleteByFoodCountDateBefore(LocalDate date);

    @Query(value = """ 
            SELECT g.*, f.*
            FROM groots g
            JOIN groots_food_history f ON g.groot_id = f.groot_id;
            """, nativeQuery = true)
    List<Object[]> getAllRecordedDataWithFoodHistory();

    //~ SQL QUERY ========================================================================================================================
}
