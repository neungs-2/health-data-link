package com.healthcare.link.service;

import com.healthcare.link.common.constant.DateTimeConstant;
import com.healthcare.link.common.error.ErrorCode;
import com.healthcare.link.common.error.exception.InternalParamException;
import com.healthcare.link.domain.entity.DailySummary;
import com.healthcare.link.domain.entity.MonthlySummary;
import com.healthcare.link.domain.entity.Source;
import com.healthcare.link.domain.entity.StepsRecord;
import com.healthcare.link.domain.entity.User;
import com.healthcare.link.dto.param.StepsEntrySumDto;
import com.healthcare.link.dto.param.StepsEntryValueDto;
import com.healthcare.link.dto.request.StepsRecordRequestDto;
import com.healthcare.link.dto.request.StepsRecordRequestDto.StepsEntryDto;
import com.healthcare.link.dto.response.DailySummaryResponseDto;
import com.healthcare.link.dto.response.MonthlySummaryResponseDto;
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
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        String recordkey = requestDto.recordkey();
        String defaultTimezoneOffset = DateTimeConstant.KST_ZONE_OFFSET;

        // 존재하는 유저인지 판단
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST_USER));

        // List 유효성 검사 (null, empty 체크)
        List<StepsEntryDto> entries = Optional.ofNullable(requestDto.data().entries())
                .filter(list -> !list.isEmpty())
                .orElseThrow(InternalParamException::new);

        // Source 저장
        Source source = healthRecordMapper.toSource(requestDto.data().source(), recordkey, user);
        sourceRepository.save(source);

        // StepsRecord 저장
        List<StepsRecord> stepsRecords = new ArrayList<>();
        for (StepsEntryDto entry : entries) {
            StepsRecord stepsRecord = healthRecordMapper.toStepsRecord(entry, source);
            stepsRecords.add(stepsRecord);
        }
        stepsRecordRepository.saveAll(stepsRecords);

        // Entry 형변환 및 날짜별 그룹화
        Map<LocalDate, List<StepsEntryValueDto>> dailyGroups = entries.stream()
                .map(healthRecordMapper::toStepsEntryValueDto)
                .collect(Collectors.groupingBy(entry -> entry.periodFrom().toLocalDate()));

        // DailySummary 계산 및 저장
        List<DailySummary> dailySummaries = calculateDailySummaries(dailyGroups, user, recordkey, defaultTimezoneOffset);
        dailySummaryRepository.saveAll(dailySummaries);

        // Entry 형변환 및 월별 그룹화
        Map<YearMonth, List<StepsEntryValueDto>> monthlyGroups = entries.stream()
                .map(healthRecordMapper::toStepsEntryValueDto)
                .collect(Collectors.groupingBy(entry -> YearMonth.from(entry.periodFrom())));

        // MonthlySummary 계산 및 저장
        List<MonthlySummary> monthlySummaries = calculateMonthlySummaries(monthlyGroups, user, recordkey, defaultTimezoneOffset);
        monthlySummaryRepository.saveAll(monthlySummaries);
    }

    @Transactional
    public List<DailySummaryResponseDto> getDailySummaries(String recordkey, String timezone, Long userId) {
        // 존재하는 유저인지 판단
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST_USER));

        // DailySummary 조회
        List<DailySummary> dailySummaries = dailySummaryRepository.findByIdRecordkeyAndIdUserId(recordkey, userId);

        // 타임존이 다른 경우 재계산
        // recordkey 기준 조회이므로 List 내 모든 원소의 timezone 동일 -> 첫 번째 원소의 timezone만 확인
        if (!dailySummaries.isEmpty() && !dailySummaries.get(0).getTimezone().equals(timezone)) {
            List<DailySummary> modifiedDailySummaries = modifyDailySummariesTimezone(recordkey, timezone, user);
            dailySummaryRepository.deleteAll(dailySummaries);
            dailySummaryRepository.saveAll(modifiedDailySummaries);

            dailySummaries = modifiedDailySummaries;
        }

        return dailySummaries.stream()
                .map(healthRecordMapper::toDailySummaryResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<MonthlySummaryResponseDto> getMonthlySummaries(String recordkey, String timezone, Long userId) {
        // 존재하는 유저인지 판단
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.NOT_EXIST_USER));

        // MonthlySummary 조회
        List<MonthlySummary> monthlySummaries = monthlySummaryRepository.findByIdUserIdAndIdRecordkey(userId, recordkey);

        // 타임존이 다른 경우 재계산
        // recordkey 기준 조회이므로 List 내 모든 원소의 timezone 동일 -> 첫 번째 원소의 timezone만 확인
        if (!monthlySummaries.isEmpty() && !monthlySummaries.get(0).getTimezone().equals(timezone)) {
            List<MonthlySummary> modifiedMonthlySummaries = modifyMonthlySummariesTimezone(recordkey, timezone, user);
            monthlySummaryRepository.deleteAll(monthlySummaries);
            monthlySummaryRepository.saveAll(modifiedMonthlySummaries);

            monthlySummaries = modifiedMonthlySummaries;
        }

        return monthlySummaries.stream()
                .map(healthRecordMapper::toMonthlySummaryResponseDto)
                .collect(Collectors.toList());
    }

    private List<DailySummary> modifyDailySummariesTimezone(String recordkey, String timezone, User user) {
        // StepsRecord 조회
        List<StepsRecord> stepsRecords = stepsRecordRepository.findByUserIdAndRecordkey(user.getUserId(), recordkey);

        // timezone 변경 적용한 entry List 생성
        Map< LocalDate, List<StepsEntryValueDto>> entries = stepsRecords.stream()
                .map(record -> new StepsEntryValueDto(
                        record.getSteps(),
                        record.getDistance(),
                        record.getCalories(),
                        record.getPeriodFrom().withZoneSameInstant(ZoneOffset.of(timezone)) // 타임존 변환
                ))
                .collect(Collectors.groupingBy(entry -> entry.periodFrom().toLocalDate()));

        // DailySummary 계산
        return calculateDailySummaries(entries, user, recordkey, timezone);
    }

    private List<MonthlySummary> modifyMonthlySummariesTimezone(String recordkey, String timezone, User user) {
        // StepsRecord 조회
        List<StepsRecord> stepsRecords = stepsRecordRepository.findByUserIdAndRecordkey(user.getUserId(), recordkey);

        // timezone 변경 적용한 entry List 생성
        Map<YearMonth, List<StepsEntryValueDto>> entries = stepsRecords.stream()
                .map(record -> new StepsEntryValueDto(
                        record.getSteps(),
                        record.getDistance(),
                        record.getCalories(),
                        record.getPeriodFrom().withZoneSameInstant(ZoneOffset.of(timezone)) // 타임존 변환
                ))
                .collect(Collectors.groupingBy(entry -> YearMonth.from(entry.periodFrom())));

        // MonthlySummary 계산
        return calculateMonthlySummaries(entries, user, recordkey, timezone);
    }

    private List<DailySummary> calculateDailySummaries(
            Map<LocalDate, List<StepsEntryValueDto>> dailyGroups,
            User user, 
            String recordkey,
            String timezoneOffset
    ) {
        List<DailySummary> summaryList = new ArrayList<>();

        //날짜별 DailySummary 생성
        for (List<StepsEntryValueDto> dailyEntries : dailyGroups.values()) {
            // 동일 날짜 검증 (period.from 기준)
            LocalDate baseDate = dailyEntries.get(0).periodFrom().toLocalDate();
            boolean allSameDay = dailyEntries.stream()
                    .allMatch(e -> e.periodFrom().toLocalDate().equals(baseDate));
            if (!allSameDay) {
                throw new InternalParamException("동일하지 않은 날짜의 데이터가 포함되었습니다.");
            }

            // 일별 합산값 계산 및 DailySummary 매핑
            StepsEntrySumDto sumResult = sumStepsEntries(dailyEntries);
            DailySummary dailySummary = healthRecordMapper.toDailySummary(sumResult, timezoneOffset, recordkey, baseDate, user);
            summaryList.add(dailySummary);
        }

        return summaryList;
    }

    private List<MonthlySummary> calculateMonthlySummaries(
            Map<YearMonth, List<StepsEntryValueDto>> monthlyGroups,
            User user,
            String recordkey,
            String timezoneOffset
    ) {
        List<MonthlySummary> summaryList = new ArrayList<>();

        // 월별 MonthlySummary 생성
        for (List<StepsEntryValueDto> monthlyEntries : monthlyGroups.values()) {
            // 동일 월 검증 (period.from 기준)
            YearMonth baseMonth = YearMonth.from(monthlyEntries.get(0).periodFrom());
            boolean allSameMonth = monthlyEntries.stream()
                    .allMatch(e -> YearMonth.from(e.periodFrom()).equals(baseMonth));
            if (!allSameMonth) {
                throw new InternalParamException("동일하지 않은 월의 데이터가 포함되었습니다.");
            }

            // 월별 합산값 계산 및 MonthlySummary 매핑
            StepsEntrySumDto sumResult = sumStepsEntries(monthlyEntries);
            MonthlySummary monthlySummary = healthRecordMapper.toMonthlySummary(sumResult, timezoneOffset, recordkey, baseMonth.toString(), user);
            summaryList.add(monthlySummary);
        }

        return summaryList;
    }

    private StepsEntrySumDto sumStepsEntries(List<StepsEntryValueDto> entryValues) {
        int totalSteps = entryValues.stream().mapToInt(e -> e.steps() != null ? e.steps() : 0).sum();
        double totalCalories = entryValues.stream().mapToDouble(e -> e.caloriesValue() != null ? e.caloriesValue() : 0.0).sum();
        double totalDistance = entryValues.stream().mapToDouble(e -> e.distanceValue() != null ? e.distanceValue() : 0.0).sum();

        return new StepsEntrySumDto(totalSteps, totalCalories, totalDistance);
    }
}
