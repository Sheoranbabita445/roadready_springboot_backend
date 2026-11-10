package com.springboot.roadready.repository;

import com.springboot.roadready.dto.response.RentalAgentResponseDto;
import com.springboot.roadready.model.RentalAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalAgentRepository extends JpaRepository<RentalAgent, Long> {

    // find a rental agent by username
    @Query("""
            SELECT ra
            FROM RentalAgent ra
            JOIN ra.user u
            WHERE u.username = ?1
            """)
    RentalAgent getByUsername(String username);

    // All rental agents (record id, employee code, username) for the admin's car form
    @Query("""
            SELECT new com.springboot.roadready.dto.response.RentalAgentResponseDto(
                ra.id, ra.employeeCode, u.username, u.active
            )
            FROM RentalAgent ra
            JOIN ra.user u
            ORDER BY ra.employeeCode
            """)
    List<RentalAgentResponseDto> getAllRentalAgents();
}
