package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Centro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CentroRepository extends JpaRepository<Centro, Integer> {}
