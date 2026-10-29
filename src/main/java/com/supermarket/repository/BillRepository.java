package com.supermarket.repository;

import com.supermarket.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {
    
    Optional<Bill> findByBillNumber(String billNumber);
    
    List<Bill> findByCustomerPhone(String customerPhone);
    
    List<Bill> findByDateTimeBetweenOrderByDateTimeDesc(LocalDateTime start, LocalDateTime end);
    
    List<Bill> findAllByOrderByDateTimeDesc();
    
    @Query("SELECT COUNT(b) FROM Bill b")
    long countTotalBills();
    
    @Query("SELECT COALESCE(SUM(b.grandTotal), 0.0) FROM Bill b")
    double sumTotalRevenue();
}

