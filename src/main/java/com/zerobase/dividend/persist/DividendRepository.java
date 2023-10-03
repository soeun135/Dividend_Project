package com.zerobase.dividend.persist;

import com.zerobase.dividend.persist.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, Long> {
    List<DividendEntity> findAllByCompanyId(Long id);

    boolean existsByCompanyIdAndDate(Long companyId, LocalDateTime date);
    //복합 유니크 키 생성해주었기 때문에 레코드를 조회하게 되면 일반 select where절보다 훨씬 빠르게 조회 가능.
}
