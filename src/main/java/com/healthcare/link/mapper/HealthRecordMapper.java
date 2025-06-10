package com.healthcare.link.mapper;

import com.healthcare.link.domain.entity.DailySummary;
import com.healthcare.link.domain.entity.MonthlySummary;
import com.healthcare.link.domain.entity.Source;
import com.healthcare.link.domain.entity.StepsRecord;
import com.healthcare.link.domain.entity.User;
import com.healthcare.link.domain.vo.DailySummaryId;
import com.healthcare.link.domain.vo.MonthlySummaryId;
import com.healthcare.link.domain.vo.SourceId;
import com.healthcare.link.dto.param.StepsEntrySumDto;
import com.healthcare.link.dto.param.StepsEntryValueDto;
import com.healthcare.link.dto.request.StepsRecordRequestDto.StepsEntryDto;
import com.healthcare.link.dto.request.StepsRecordRequestDto.StepsRecordDataDto.SourceDto;
import com.healthcare.link.dto.response.DailySummaryResponseDto;
import com.healthcare.link.dto.response.MonthlySummaryResponseDto;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

    public StepsEntryValueDto toStepsEntryValueDto(StepsEntryDto stepsEntryDto) {
        return new StepsEntryValueDto(
                stepsEntryDto.steps(),
                stepsEntryDto.distance().value(),
                stepsEntryDto.calories().value(),
                stepsEntryDto.period().from()
        );
    }

    public DailySummary toDailySummary(
            StepsEntrySumDto dailySum,
            String timezoneOffset,
            String recordkey,
            LocalDate localDate,
            User user
    ) {
        return DailySummary.builder()
                .id(new DailySummaryId(user.getUserId(), recordkey, localDate))
                .user(user)
                .timezone(timezoneOffset)
                .steps(dailySum.totalSteps())
                .calories(dailySum.totalCalories())
                .distance(dailySum.totalDistance())
                .build();
    }

    public MonthlySummary toMonthlySummary(
            StepsEntrySumDto monthlySum,
            String timezoneOffset,
            String recordkey,
            String yearMonth,
            User user
    ) {
        return MonthlySummary.builder()
                .id(new MonthlySummaryId(user.getUserId(), recordkey, yearMonth))
                .user(user)
                .timezone(timezoneOffset)
                .steps(monthlySum.totalSteps())
                .calories(monthlySum.totalCalories())
                .distance(monthlySum.totalDistance())
                .build();
    }

    public DailySummaryResponseDto toDailySummaryResponseDto(DailySummary dailySummary) {
        return new DailySummaryResponseDto(
                dailySummary.getId().date(),
                dailySummary.getSteps(),
                dailySummary.getCalories(),
                dailySummary.getDistance(),
                dailySummary.getId().recordkey(),
                dailySummary.getUser().getUserId(),
                dailySummary.getTimezone()
        );
    }

    public MonthlySummaryResponseDto toMonthlySummaryResponseDto(MonthlySummary monthlySummary) {
        return new MonthlySummaryResponseDto(
                monthlySummary.getId().date(),
                monthlySummary.getSteps(),
                monthlySummary.getCalories(),
                monthlySummary.getDistance(),
                monthlySummary.getId().recordkey(),
                monthlySummary.getId().userId(),
                monthlySummary.getTimezone()
        );
    }

}
