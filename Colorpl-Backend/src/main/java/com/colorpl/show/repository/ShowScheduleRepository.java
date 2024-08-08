package com.colorpl.show.repository;

import com.colorpl.show.domain.ShowDetail;
import com.colorpl.show.domain.ShowSchedule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowScheduleRepository extends JpaRepository<ShowSchedule, Long>,
    ShowScheduleRepositoryCustom {

    List<ShowSchedule> findByShowDetail(ShowDetail showDetail);
}
