package com.zerobase.dividend.persist;

import com.zerobase.dividend.persist.entity.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    boolean existsByTicker(String ticker);

    Optional<CompanyEntity> findByName(String name);
    //OPtional로 받아주는 이유
    //NullPointerException 방지
    //값이 없는 경우의 처리도 깔끔하게 처리 가능

    Page<CompanyEntity> findByNameStartingWithIgnoreCase(String s, Pageable pageable);

}
