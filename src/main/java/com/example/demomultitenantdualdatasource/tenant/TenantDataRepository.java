package com.example.demomultitenantdualdatasource.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TenantDataRepository extends JpaRepository<TenantData, Long> {
}