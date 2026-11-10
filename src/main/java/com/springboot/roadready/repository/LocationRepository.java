package com.springboot.roadready.repository;

import com.springboot.roadready.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

    // locations for the dropdowns and the admin Locations page
    @Query("""
            SELECT l
            FROM Location l
            ORDER BY l.city, l.address
            """)
    List<Location> getAllLocationsSorted();
}
