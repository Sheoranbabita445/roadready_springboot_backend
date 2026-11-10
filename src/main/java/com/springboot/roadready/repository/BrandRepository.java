package com.springboot.roadready.repository;

import com.springboot.roadready.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    // used to stop duplicate brand names (the column is unique)
    @Query("""
            SELECT COUNT(b)
            FROM Brand b
            WHERE LOWER(b.name) = LOWER(?1)
            """)
    Long countByNameIgnoreCase(String name);

    // brands for the dropdowns, alphabetical
    @Query("""
            SELECT b
            FROM Brand b
            ORDER BY b.name
            """)
    List<Brand> getAllBrandsSorted();
}
