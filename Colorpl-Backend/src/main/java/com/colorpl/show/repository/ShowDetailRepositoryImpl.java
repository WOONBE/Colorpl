package com.colorpl.show.repository;

import static com.colorpl.show.domain.QShowDetail.showDetail;
import static com.colorpl.show.domain.QShowSchedule.showSchedule;

import com.colorpl.show.domain.Category;
import com.colorpl.show.domain.ShowDetail;
import com.colorpl.show.dto.GetShowDetailsByConditionRequest;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShowDetailRepositoryImpl implements ShowDetailRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ShowDetail> getShowDetailsByCondition(GetShowDetailsByConditionRequest request) {
        return queryFactory
            .select(showDetail).distinct()
            .from(showDetail)
            .join(showDetail.showSchedules, showSchedule).fetchJoin()
            .where(
                dateEq(request.getDate()),
                areaEq(request.getArea()),
                nameContains(request.getKeyword()),
                categoryEq(request.getCategory())
            )
            .fetch();
    }

    @Override
    public ShowDetail findShowDetailAndShowSchedulesById(Integer id) {
        return queryFactory
            .select(showDetail).distinct()
            .from(showDetail)
            .join(showDetail.showSchedules, showSchedule).fetchJoin()
            .where(showDetail.id.eq(id))
            .fetchOne();
    }

//    @Override
//    public ShowDetail findShowDetailAndSeatsById(Integer id) {
//        return queryFactory
//            .select(showDetail).distinct()
//            .from(showDetail)
//            .join(showDetail.seats, seat).fetchJoin()
//            .where(showDetail.id.eq(id))
//            .fetchOne();
//    }

    private BooleanExpression dateEq(LocalDate date) {
        if (date == null) {
            return null;
        }
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        return showSchedule.dateTime.between(from, to);
    }

    private BooleanExpression areaEq(String area) {
        return area != null ? showDetail.area.eq(area) : null;
    }

    private BooleanExpression nameContains(String keyword) {
        return keyword != null ? showDetail.name.contains(keyword) : null;
    }

    private BooleanExpression categoryEq(Category category) {
        return category != null ? showDetail.category.eq(category) : null;
    }
}
