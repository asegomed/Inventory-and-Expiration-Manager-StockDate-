package com.example.stockdate.persistence.repository;

import com.example.stockdate.persistence.entity.BatchEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<BatchEntity, Long> {

    List<BatchEntity> findAllByOrderByExpirationDateAscIdAsc();
}
