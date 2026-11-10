package com.springboot.roadready.repository;

import com.springboot.roadready.dto.response.AdminUserResponseDto;
import com.springboot.roadready.dto.response.UserDetailsDto;
import com.springboot.roadready.enums.Role;
import com.springboot.roadready.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // find user by username for authentication
    @Query("""
            SELECT u FROM User u
            WHERE u.username = ?1
            """)
    UserDetails loadUserByUsername(String username);

    // Users for the admin (customers and rental agents)
    @Query("""
            SELECT new com.springboot.roadready.dto.response.AdminUserResponseDto(
                u.id, u.username, u.role, u.active
            )
            FROM User u
            WHERE u.role IN ?1
            AND (?2 IS NULL OR u.role = ?2)
            AND (?3 IS NULL OR LOWER(u.username) LIKE ?3)
            ORDER BY u.id DESC
            """)
    Page<AdminUserResponseDto> getUserForAdmin(
            List<Role> roles,
            Role role,
            String searchPattern,
            Pageable pageable
    );


    @Query("""
           select new com.springboot.roadready.dto.response.UserDetailsDto(
           u.username, u.active, u.role
           )
           from User u
           where u.username=?1
           """)
    UserDetailsDto getUserDetails(String loggedInUsername);

    // number of accounts with a role (admin dashboard cards)
    long countByRole(Role role);
}
