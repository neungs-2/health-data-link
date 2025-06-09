package com.healthcare.link.service;

import com.healthcare.link.common.error.ErrorCode;
import com.healthcare.link.domain.entity.DailySummary;
import com.healthcare.link.domain.entity.MonthlySummary;
import com.healthcare.link.domain.entity.Source;
import com.healthcare.link.domain.entity.StepsRecord;
import com.healthcare.link.domain.entity.User;
import com.healthcare.link.dto.request.StepsRecordRequestDto;
import com.healthcare.link.common.error.exception.ResourceNotFoundException;
import com.healthcare.link.mapper.HealthRecordMapper;
import com.healthcare.link.repository.DailySummaryRepository;
import com.healthcare.link.repository.MonthlySummaryRepository;
import com.healthcare.link.repository.SourceRepository;
import com.healthcare.link.repository.StepsRecordRepository;
import com.healthcare.link.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class HealthRecordService {
    private final UserRepository userRepository;
    private final SourceRepository sourceRepository;
    private final StepsRecordRepository stepsRecordRepository;
    private final DailySummaryRepository dailySummaryRepository;
    private final MonthlySummaryRepository monthlySummaryRepository;
    private final HealthRecordMapper healthRecordMapper;

    @Transactional
    public void saveSteps(StepsRecordRequestDto requestDto, Long userId) {
        // 존재하는 유저인지 판단
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST_USER));

        List<StepsRecordRequestDto.StepsEntryDto> entries = requestDto.data().entries();
        String timezoneOffset = entries.get(0).period().from().getOffset().toString();

        // Source 저장
        Source source = healthRecordMapper.toSource(requestDto.data().source(), requestDto.recordkey(), user);
        sourceRepository.save(source);

        // StepsRecord 저장
        List<StepsRecord> stepsRecords = new ArrayList<>();
        for (StepsRecordRequestDto.StepsEntryDto entry : entries) {
            StepsRecord stepsRecord = healthRecordMapper.toStepsRecord(entry, source);
            stepsRecords.add(stepsRecord);
        }
        stepsRecordRepository.saveAll(stepsRecords);


        // 날짜별로 그룹화하여 DailySummary 생성
        Map<LocalDate, List<StepsRecordRequestDto.StepsEntryDto>> dailyGroups = entries.stream()
                .collect(Collectors.groupingBy(entry -> entry.period().from().toLocalDate()));

        List<DailySummary> dailySummaries = dailyGroups.values().stream()
                .map(dailyEntries -> healthRecordMapper.toDailySummary(dailyEntries, timezoneOffset, requestDto.recordkey(), user))
                .collect(Collectors.toList());

        // 월별로 그룹화하여 MonthlySummary 생성
        Map<YearMonth, List<StepsRecordRequestDto.StepsEntryDto>> monthlyGroups = entries.stream()
                .collect(Collectors.groupingBy(entry -> YearMonth.from(entry.period().from())));

        List<MonthlySummary> monthlySummaries = monthlyGroups.values().stream()
                .map(monthlyEntries -> healthRecordMapper.toMonthlySummary(monthlyEntries, timezoneOffset, requestDto.recordkey(), user))
                .collect(Collectors.toList());

        // 일괄 저장
        dailySummaryRepository.saveAll(dailySummaries);
        monthlySummaryRepository.saveAll(monthlySummaries);
    }
}
