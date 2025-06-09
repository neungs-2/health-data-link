package com.healthcare.link.mapper;

import com.healthcare.link.common.error.exception.InternalParamException;
import com.healthcare.link.domain.entity.DailySummary;
import com.healthcare.link.domain.entity.MonthlySummary;
import com.healthcare.link.domain.entity.Source;
import com.healthcare.link.domain.entity.StepsRecord;
import com.healthcare.link.domain.entity.User;
import com.healthcare.link.domain.vo.DailySummaryId;
import com.healthcare.link.domain.vo.MonthlySummaryId;
import com.healthcare.link.domain.vo.SourceId;
import com.healthcare.link.dto.request.StepsRecordRequestDto.StepsEntryDto;
import com.healthcare.link.dto.request.StepsRecordRequestDto.StepsRecordDataDto.SourceDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Component
public class HealthRecordMapper {
    public Source toSource(SourceDto sourceDto, String recordkey, User user) {
        return Source.builder()
                .id(new SourceId(recordkey, user.getUserId()))
                .user(user)
                .name(sourceDto.name())
                .productName(sourceDto.product().name())
                .productVender(sourceDto.product().vender())
                .mode(sourceDto.mode())
                .type(sourceDto.type())
                .build();
    }

    public StepsRecord toStepsRecord(StepsEntryDto stepsEntryDto, Source source) {
        return StepsRecord.builder()
                .steps(stepsEntryDto.steps())
                .source(source)
                .calories(stepsEntryDto.calories().value())
                .distance(stepsEntryDto.distance().value())
                .periodFrom(stepsEntryDto.period().from())
                .periodTo(stepsEntryDto.period().to())
                .build();
    }

    public DailySummary toDailySummary(List<StepsEntryDto> dailyEntries, String timezoneOffset, String recordkey, User user) {
        if (dailyEntries == null || dailyEntries.isEmpty()) {
            throw new InternalParamException();
        }

        // 동일 날짜 검증 (period.from 기준)
        LocalDate baseDate = dailyEntries.get(0).period().from().toLocalDate();
        boolean allSameDay = dailyEntries.stream()
                .allMatch(e -> e.period().from().toLocalDate().equals(baseDate));
        if (!allSameDay) {
            throw new InternalParamException("동일하지 않은 날짜의 데이터가 포함되었습니다.");
        }

        int totalSteps = dailyEntries.stream().mapToInt(e -> e.steps() != null ? e.steps() : 0).sum();
        double totalCalories = dailyEntries.stream().mapToDouble(e -> e.calories() != null ? e.calories().value() : 0.0).sum();
        double totalDistance = dailyEntries.stream().mapToDouble(e -> e.distance() != null ? e.distance().value() : 0.0).sum();

        return DailySummary.builder()
                .id(new DailySummaryId(user.getUserId(), recordkey, baseDate))
                .user(user)
                .timezone(timezoneOffset)
                .steps(totalSteps)
                .calories(totalCalories)
                .distance(totalDistance)
                .build();
    }

    public MonthlySummary toMonthlySummary(List<StepsEntryDto> monthlyEntries, String timezoneOffset, String recordkey, User user) {
        if (monthlyEntries == null || monthlyEntries.isEmpty()) {
            throw new InternalParamException();
        }

        // 동일 월 검증 (period.from 기준)
        YearMonth baseMonth = YearMonth.from(monthlyEntries.get(0).period().from());
        boolean allSameMonth = monthlyEntries.stream()
                .allMatch(e -> YearMonth.from(e.period().from()).equals(baseMonth));
        if (!allSameMonth) {
            throw new InternalParamException("동일하지 않은 월의 데이터가 포함되었습니다.");
        }

        int totalSteps = monthlyEntries.stream().mapToInt(e -> e.steps() != null ? e.steps() : 0).sum();
        double totalCalories = monthlyEntries.stream().mapToDouble(e -> e.calories() != null ? e.calories().value() : 0.0).sum();
        double totalDistance = monthlyEntries.stream().mapToDouble(e -> e.distance() != null ? e.distance().value() : 0.0).sum();

        return MonthlySummary.builder()
                .id(new MonthlySummaryId(user.getUserId(), recordkey, baseMonth.toString()))
                .user(user)
                .timezone(timezoneOffset)
                .steps(totalSteps)
                .calories(totalCalories)
                .distance(totalDistance)
                .build();
    }
}
