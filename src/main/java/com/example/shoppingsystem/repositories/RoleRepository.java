package com.example.shoppingsystem.repositories;

import com.example.shoppingsystem.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Role findByRoleCode(String roleCode);
    List<Role> findByRoleCodeIn(List<String> roleCodes);
}
