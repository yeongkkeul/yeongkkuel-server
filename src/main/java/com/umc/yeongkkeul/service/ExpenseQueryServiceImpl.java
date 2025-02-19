package com.umc.yeongkkeul.service;

import com.umc.yeongkkeul.apiPayload.code.status.ErrorStatus;
import com.umc.yeongkkeul.apiPayload.exception.handler.ExpenseHandler;
import com.umc.yeongkkeul.apiPayload.exception.handler.UserHandler;
import com.umc.yeongkkeul.converter.CategoryConverter;
import com.umc.yeongkkeul.domain.Category;
import com.umc.yeongkkeul.domain.Expense;
import com.umc.yeongkkeul.domain.Reward;
import com.umc.yeongkkeul.domain.User;
import com.umc.yeongkkeul.domain.enums.AgeGroup;
import com.umc.yeongkkeul.domain.enums.Job;
import com.umc.yeongkkeul.repository.CategoryRepository;
import com.umc.yeongkkeul.repository.ExpenseRepository;
import com.umc.yeongkkeul.repository.RewardRepository;
import com.umc.yeongkkeul.repository.UserRepository;
import com.umc.yeongkkeul.web.dto.CategoryResponseDTO;
import com.umc.yeongkkeul.web.dto.ExpenseResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseQueryServiceImpl implements ExpenseQueryService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final RewardRepository rewardRepository;

    // 일간 - 유저의 하루 목쵸 지출액 조회
    @Override
    public ExpenseResponseDTO.DayTargetExpenditureViewDTO DayTargetExpenditureViewDTO(Long userId) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 만약 유저가 하루 목표 지출액을 설정해둔 적이 없다면 에러
        if (user.getDayTargetExpenditure() == null) {
            throw new ExpenseHandler(ErrorStatus.EXPENSE_DAY_TARGET_EXPENDITURE_NOT_FOUND);
        }

        return new ExpenseResponseDTO.DayTargetExpenditureViewDTO().builder()
                .dayTargetExpenditure(user.getDayTargetExpenditure())
                .build();
    }

    // 일간 - 카테고리별 지출 기록(목록) 조회
    @Override
    public ExpenseResponseDTO.CategoryListExpenditureViewDTO CategoryExpenseListView(Long userId, Integer year, Integer month, Integer day){
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 카테고리 및 지출 정보 가져오기 (이때 year, month, day 넘기기)
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());
        List<CategoryResponseDTO.CategoryViewListWithExpenditureDTO> categoryList
                = CategoryConverter.toCategoriesViewListWithExpenditureDTO(categories, user, year, month, day);

        return ExpenseResponseDTO.CategoryListExpenditureViewDTO.builder()
                .categories(categoryList)
                .build();
    }

    // 주간 - 총 지출액 조회
    @Override
    public ExpenseResponseDTO.WeeklyExpenditureViewDTO getWeeklyExpenditure(Long userId) {
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 오늘 날짜 구하기
        LocalDate today = LocalDate.now();

        // 이번 주 월요일 구하기
        LocalDate startDay = today.with(DayOfWeek.MONDAY);
        // 이번 주 일요일 구하기
        LocalDate endDay = today.with(DayOfWeek.SUNDAY);

        // 일주일 총 지출액 계산
        int weekExpenditure = 0;

        // 해당 유저의 지출 내역을 월요일부터 오늘까지 필터링해서 가져오기
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDayAtBetween(user.getId(), startDay, today);

        // 월요일부터 오늘까지의 각 요일별 지출 금액 계산
        List<ExpenseResponseDTO.ExpensePerDayDTO> expensesPerDay = new ArrayList<>();

        // 월~일요일 각 요일별로 지출액을 계산
        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            // 요일 구하기
            LocalDate currentDay = startDay.with(DayOfWeek.of(dayOfWeek.getValue()));

            // 요일이 오늘 날짜 이후라면 지출액을 0으로 설정
            if (currentDay.isAfter(today)) {
                expensesPerDay.add(new ExpenseResponseDTO.ExpensePerDayDTO(
                        currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd, EEEE")), // "2025-01-11, Saturday"
                        null // 아직 도래하지 않은 요일은 null로 설정
                ));
                continue; // 해당 요일은 더 이상 계산하지 않고 넘으
            }

            // 해당 요일에 해당하는 유저의 지출 금액 구하기
            List<Expense> dailyExpenses = expenses.stream()
                    .filter(expense -> expense.getUser().getId().equals(user.getId()) && expense.getDay().equals(currentDay)) // 유저와 날짜 필터링
                    .collect(Collectors.toList());

            int dailyExpenditure;

            // 지출 내역이 없다면 -1로 설정한 다음 null로 설정
            if (dailyExpenses.isEmpty()) {
                dailyExpenditure = -1; // 지출이 없으면 -1로 설정
            } else {
                // 의도적으로 0을 입력한 경우
                dailyExpenditure = dailyExpenses.stream()
                        .mapToInt(Expense::getAmount)
                        .sum();
            }

            // 해당 요일의 지출 내역을 리스트에 추가
            if (dailyExpenditure == -1) {
                expensesPerDay.add(new ExpenseResponseDTO.ExpensePerDayDTO(
                        currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd, EEEE")),
                        null // 지출이 없을 경우 null로 설정
                ));
            } else {
                expensesPerDay.add(new ExpenseResponseDTO.ExpensePerDayDTO(
                        currentDay.format(DateTimeFormatter.ofPattern("yyyy-MM-dd, EEEE")),
                        dailyExpenditure
                ));
            }

            // 주간 지출액 계산
            if (dailyExpenditure != -1) {
                weekExpenditure += dailyExpenditure;
            }
        }

        // 하루 목표 지출액이 null일 경우의 반환
        if (user.getDayTargetExpenditure() == null){
            return ExpenseResponseDTO.WeeklyExpenditureViewDTO.builder()
                    .weekExpenditure(weekExpenditure)
                    // .dayTargetExpenditure(dayTargetExpenditure)
                    .expenses(expensesPerDay)
                    .build();
        }

        // 응답 반환
        return ExpenseResponseDTO.WeeklyExpenditureViewDTO.builder()
                .weekExpenditure(weekExpenditure)
                .dayTargetExpenditure(user.getDayTargetExpenditure())
                .expenses(expensesPerDay)
                .build();
    }

    // 주간 - 해당 주간의 지출 통계 조회하기
    @Override
    public ExpenseResponseDTO.WeeklyAverageExpenditureViewDTO weeklyAverageExpenditureViewDTO(Long userId){
        // 유저 찾기 -> 유저의 나이대, 직장인 구할거임
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        // 이번주 총 지출액
        LocalDate today = LocalDate.now();
        LocalDate startDay = today.with(DayOfWeek.MONDAY);
        LocalDate endDay = today.with(DayOfWeek.SUNDAY);
        int thisWeekExpenditure = totalWeeklyExpenditure(user, startDay, endDay);

        // 저번주 총 지출액
        int lastWeekExpenditure = totalWeeklyExpenditure(user, startDay.minusDays(7), endDay.minusDays(7));

        // 이번주 카테고리별 지출 총액 가져오기
        List<Category> categories = categoryRepository.findAllByUserId(user.getId());
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDayAtBetween(user.getId(), startDay, endDay);

        // 이번주 지출액이 개 큰 categoryName - categoryConverter 이용
        String hightestExpenditureCategoryName = CategoryConverter.getCategoryWithHighestExpenditure(user, categories, expenses, startDay, endDay);

        // 이번주 카테고리별 지출 내역 - categoryConverter 이용
        List<CategoryResponseDTO.CategoryViewListWithWeeklyExpenditureDTO> categoryList
                = CategoryConverter.categoryViewListWithWeeklyExpenditureDTO(categories, expenses, user, startDay, endDay);

        // 사용자가 직접 지출을 작성한 날 갯수
        int numberOfExpenditureDay = dailyAverageExpenditure(expenses);

        // 하루 평균 지출액 계산
        int dailyAverage = numberOfExpenditureDay > 0 ? thisWeekExpenditure / numberOfExpenditureDay : 0;

        // 유저의 상위 백분위 구하기
        int userPercentile = calculateTopPercentile(user, startDay, endDay);

        // 유저의 동일한 나이대 및 직업 다른 유저들의 하루 평균 지출액
        int averageExpenditure = calculateAverageExpenditureForGroup(user, startDay, endDay);

        // 나이대와 직업이 둘 다 UNDECIDED인 경우 또는 사용자가 직접 지출을 작성한 날의 갯수가 일주일의 절반도 되지 않을 경우, 다른 응답 객체 반환
        if (user.getAgeGroup() == AgeGroup.UNDECIDED && user.getJob() == Job.UNDECIDED || numberOfExpenditureDay < 4) {
            // 여기서 나이대와 직업이 UNDECIDED일 경우 다른 응답을 리턴
            // 이때 하루 평균 지출액, 저번주에 얼마 사용, 이번주에 얼마 사용, 많이 쓴 카테고리 이름, 카테고리별 지출 내역 리스트 넘기기
            return handleUnDecidedUser(user.getAgeGroup(), user.getJob(), dailyAverage, lastWeekExpenditure, thisWeekExpenditure, hightestExpenditureCategoryName ,categoryList); // 다른 응답 포맷을 리턴하는 메서드 호출
        }

        // 나이, 직업 둘 중 하나 설정되어 있을 경우 response
        return ExpenseResponseDTO.WeeklyAverageExpenditureViewDTO.builder()
                .age(user.getAge())
                .job(user.getJob())
                .topPercent(userPercentile)
                .averageExpenditure(averageExpenditure)
                .myAverageExpenditure(dailyAverage)
                .lastWeekExpenditure(lastWeekExpenditure)
                .thisWeekExpenditure(thisWeekExpenditure)
                .highestExpenditureCategoryName(hightestExpenditureCategoryName)
                .categories(categoryList)
                .build();
    }

    // 나이대와 직업이 UNDECIDED인 경우, 다른 응답을 반환하는 메서드
    private ExpenseResponseDTO.WeeklyAverageExpenditureViewDTO handleUnDecidedUser(AgeGroup userAgeGroup, Job userJob, Integer dailyAverage, Integer lastWeekExpenditure, Integer thisWeekExpenditure, String highestExpenditureCategoryName, List<CategoryResponseDTO.CategoryViewListWithWeeklyExpenditureDTO> categoryList) {
        // UNDECIDED일 경우 반환할 다른 응답 형태
        return ExpenseResponseDTO.WeeklyAverageExpenditureViewDTO.builder()
                .age(userAgeGroup)
                .job(userJob)
                .myAverageExpenditure(dailyAverage)
                .lastWeekExpenditure(lastWeekExpenditure)
                .thisWeekExpenditure(thisWeekExpenditure)
                .highestExpenditureCategoryName(highestExpenditureCategoryName)
                .categories(categoryList)
                .build();
    }

    // 주간 - 해당 주간의 지출 통계 조회하기에서 사용하는 [주간 총 지출액] - 요걸로 이번주/저번주 총 지출액 구할거임
    public Integer totalWeeklyExpenditure(User user, LocalDate startDay, LocalDate endDay) {
        // 해당 유저의 지출 내역을 startDay부터 endDay까지 필터링해서 가져오기
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDayAtBetween(user.getId(), startDay, endDay);

        // 주간 지출액 계산
        int weeklyExpenditure = expenses.stream()
                .mapToInt(Expense::getAmount)  // 지출 금액 합산
                .sum();

        return weeklyExpenditure;
    }

    // 이번주 하루 평균 지출액 = 사용자가 지출을 작성한 날 갯수
    public Integer dailyAverageExpenditure(List<Expense> expenses) {
        // 사용자가 지출 내역을 작성한 날을 세는 로직 (하루에 여러 개의 지출 내역이 있어도 하루를 한 번만 카운트)
        return (int) expenses.stream()
                .filter(expense -> expense.getAmount() > 0 || expense.getIsNoSpending() == true) // 지출 내역을 작성한 날만 계산
                .map(expense -> expense.getDay()) // 날짜 기준으로 그룹화
                .distinct() // 같은 날은 한 번만 카운트
                .count();

        // 하루 평균 지출액 계산
        // return daysWithSpending > 0 ? thisWeekExpenditure / daysWithSpending : 0;
    }

    // 유저의 나이, 직업과 동일한 유저 그룹 찾기
    // if) 둘 다 UNDECIDED인 경우 -> 하지마
    // 나이대만 설정된 경우, 동일한 나이대에 해당하는 다른 유저 그룹 찾기
    // 직업만 설정된 경우, 동일한 직업에 해당하는 다른 유저 그룹 찾기
    public Integer calculateAverageExpenditureForGroup(User user, LocalDate startDay, LocalDate endDay){
        // 나이대와 직업 기준으로 그룹화하여 하루 평균 지출액 계산
        int averageExpenditureForGroup = 0;
        int totalUsers = 0;

        // 나이대와 직업이 모두 설정된 경우
        if (user.getAgeGroup() != AgeGroup.UNDECIDED && user.getJob() != Job.UNDECIDED) {
            List<User> usersInSameAgeAndJobGroup = userRepository.findByAgeGroupAndJob(user.getAgeGroup(), user.getJob());
            averageExpenditureForGroup = calculateGroupAverageExpenditure(usersInSameAgeAndJobGroup, startDay, endDay);
            totalUsers += usersInSameAgeAndJobGroup.size();

//            System.out.println("나이대+직업");
//            System.out.println("이 그룹에 속한 유저들의 하루 평균 지출액을 다 더한 수는 "+ averageExpenditureForGroup);
//            System.out.println("이 그룹에 속한 유저의 수는 "+ totalUsers);
        }

        // 나이대만 설정된 경우
        if (user.getAgeGroup() != AgeGroup.UNDECIDED && user.getJob() == Job.UNDECIDED) {
            List<User> usersInSameAgeGroup = userRepository.findByAgeGroup(user.getAgeGroup());
            averageExpenditureForGroup += calculateGroupAverageExpenditure(usersInSameAgeGroup, startDay, endDay);
            totalUsers += usersInSameAgeGroup.size();
        }

        // 직업만 설정된 경우
        if (user.getJob() != Job.UNDECIDED && user.getAgeGroup() == AgeGroup.UNDECIDED) {
            List<User> usersInSameJobGroup = userRepository.findByJob(user.getJob());
            averageExpenditureForGroup += calculateGroupAverageExpenditure(usersInSameJobGroup, startDay, endDay);
            totalUsers += usersInSameJobGroup.size();
        }

        // 그룹 평균 지출액 반환
        return averageExpenditureForGroup / totalUsers;
    }

    // 그룹 내 유저들의 하루 평균 지출액 다 더하기
    public Integer calculateGroupAverageExpenditure(List<User> group, LocalDate startDay, LocalDate endDay) {
        int totalExpenditure = 0;

        for (User groupUser : group) {
            List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDayAtBetween(groupUser.getId(), startDay, endDay);
            System.out.println("유저 목록 : "+ groupUser.getEmail());

            // 하루 평균 지출액 계산해서 다 더하기
            totalExpenditure += calculateDailyExpenditure(expenses);
        }

        return totalExpenditure;
    }

    // 유저의 하루 평균 지출액을 계산하는 메서드
    private Integer calculateDailyExpenditure(List<Expense> expenses) {
        int totalExpenditure = expenses.stream()
                .mapToInt(Expense::getAmount)
                .sum();

        int daysWithExpenditure = (int) expenses.stream()
                .filter(expense -> expense.getAmount() > 0 || expense.getIsNoSpending() == true) // 지출 내역을 작성한 날만 계산
                .map(expense -> expense.getDay())
                .distinct()
                .count();

        // 하루 평균 지출액 계산
        return daysWithExpenditure > 0 ? totalExpenditure / daysWithExpenditure : 0;
    }

    // 유저의 백분위를 계산하는 메서드 (지출이 적을수록 상위)
    private Integer calculatePercentileForGroup(List<User> group, User user, LocalDate startDay, LocalDate endDay, int userDailyExpenditure) {
        // 그룹 내 모든 사용자의 하루 평균 지출액을 계산
        List<Integer> dailyExpenditures = group.stream()
                .map(u -> calculateDailyExpenditure(expenseRepository.findByUserIdAndExpenseDayAtBetween(u.getId(), startDay, endDay))) // 하루 평균 지출액 계산
                .collect(Collectors.toList());

        // 사용자의 하루 평균 지출액
        int userExpenditure = userDailyExpenditure;

        // 현재 사용자의 지출이 그룹 내에서 어느 위치에 있는지 구하기 (지출이 적을수록 상위)
        long rank = dailyExpenditures.stream()
                .filter(expenditure -> expenditure <= userExpenditure) // 지출이 적거나 같은 유저들만
                .count();

        // 백분위 계산: 순위 / 전체 유저 수
        return (int)Math.round((rank * 100.0) / group.size());
    }

    // 상위 백분위를 계산하는 메서드
    public Integer calculateTopPercentile(User user, LocalDate startDay, LocalDate endDay) {
        // 유저의 하루 평균 지출액을 calculateDailyExpenditure 메서드로 구합니다
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDayAtBetween(user.getId(), startDay, endDay);
        int userDailyExpenditure = calculateDailyExpenditure(expenses); // 유저의 하루 평균 지출액

        List<User> usersInSameGroup = new ArrayList<>();

        // 나이대와 직업이 모두 설정된 경우
        if (user.getAgeGroup() != AgeGroup.UNDECIDED && user.getJob() != Job.UNDECIDED) {
            usersInSameGroup = userRepository.findByAgeGroupAndJob(user.getAgeGroup(), user.getJob());
        }
        // 나이대만 설정된 경우
        else if (user.getAgeGroup() != AgeGroup.UNDECIDED && user.getJob() == Job.UNDECIDED) {
            usersInSameGroup = userRepository.findByAgeGroup(user.getAgeGroup());
        }
        // 직업만 설정된 경우
        else if (user.getAgeGroup() == AgeGroup.UNDECIDED && user.getJob() != Job.UNDECIDED) {
            usersInSameGroup = userRepository.findByJob(user.getJob());
        }

        // 동일한 나이대 및 직업을 가진 유저들의 백분위 계산
        if (usersInSameGroup.isEmpty()) {
            return 0; // 그룹에 유저가 없으면 0으로 처리
        }

        // 그룹 내 백분위 계산
        return calculatePercentileForGroup(usersInSameGroup, user, startDay, endDay, userDailyExpenditure);
    }

    // 유저의 월간 지출액 조회
    public ExpenseResponseDTO.MonthlyExpenditureViewDTO monthlyExpenditureViewDTO(Long userId, Integer year, Integer month){
        // 유저 찾기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserHandler(ErrorStatus.USER_NOT_FOUND));

        int lastYear = (month == 1) ? year-1 : year;
        int lastMonth = (month == 1) ? 12 : month-1;

        // 선택된 그 달의 한달 누적 지출액
        // 주간 총 지출액 구할 때 썼던 함수 재사용
        int thisMonthExpenditure = totalWeeklyExpenditure(user, LocalDate.of(year, month, 1), LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()));

        // 선택된 그 달에 해당되는 월, 일별 지출액 리스트
        List<ExpenseResponseDTO.MonthExpenseDTO> thisMonthExpenditureList = monthExpenseList(user, LocalDate.of(year, month, 1), LocalDate.of(year, month, 1).withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth()));

        // 선택된 그 달에 해당되는 전 달의 월, 일별 지출액 리스트
        List<ExpenseResponseDTO.MonthExpenseDTO> lastMonthExpenditureList = monthExpenseList(user, LocalDate.of(lastYear, lastMonth, 1), LocalDate.of(lastYear, lastMonth, 1).withDayOfMonth(LocalDate.of(lastYear, lastMonth, 1).lengthOfMonth()));

        // 이번달에 받은 리워드 목록
        List<Reward> rewards = rewardRepository.findByUserIdAndYearAndMonth(user.getId(), year, month);

        int thisMonthReward = rewards.stream()
                .mapToInt(Reward::getAmount)  // 각 리워드 금액 합산
                .sum();

        // 만약 하루 목표 지출액을 설정하지 않은 유저라면, 목표 달성 일수 제외 반환하기
        if (user.getDayTargetExpenditure() == null){
            return ExpenseResponseDTO.MonthlyExpenditureViewDTO.builder()
                    .totalMonthExpenditure(thisMonthExpenditure)
                    .selectedMonthExpenses(thisMonthExpenditureList)
                    .previousMonthExpenses(lastMonthExpenditureList)
                    .rewards(thisMonthReward)
                    .build();
        }

        // 목표 달성 일수 = 하루 목표 지출액 >= 하루 지출액 일 경우 목표 달성
        int achieveDays = calculateAchieveDays(user, thisMonthExpenditureList);

        return ExpenseResponseDTO.MonthlyExpenditureViewDTO.builder()
                .dayTargetExpenditure(user.getDayTargetExpenditure())
                .totalMonthExpenditure(thisMonthExpenditure)
                .selectedMonthExpenses(thisMonthExpenditureList)
                .previousMonthExpenses(lastMonthExpenditureList)
                .achieveDays(achieveDays)
                .rewards(thisMonthReward)
                .build();
    }

    // 선택된 달에 해당되는 월, 일별 지출액 리스트
    // user, startDay, endDay 넘겨
    public List<ExpenseResponseDTO.MonthExpenseDTO> monthExpenseList(User user, LocalDate startDay, LocalDate endDay) {
        // 해당 월의 일별 지출액 리스트 (지출 내역이 있는 날만)
        List<ExpenseResponseDTO.MonthExpenseDTO> monthlyExpenditureList = new ArrayList<>();

        // 지출 내역 가져오기
        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDayAtBetween(user.getId(), startDay, endDay);

        // 지출 내역이 있는 날만 리스트에 추가할거고
        // 날짜별로 지출액을 합산
        expenses.stream()
                .collect(Collectors.groupingBy(Expense::getDay))  // 날짜별로 그룹화
                .forEach((date, dailyExpenses) -> {
                    int dailyExpenditure = dailyExpenses.stream()
                            .mapToInt(Expense::getAmount)  // 해당 날짜의 지출 금액 합산
                            .sum();

                    monthlyExpenditureList.add(new ExpenseResponseDTO.MonthExpenseDTO(
                            date.getYear(),
                            date.getMonthValue(),
                            date,
                            dailyExpenditure
                    ));
                });

        // 날짜 오름차순으로 정렬
        monthlyExpenditureList.sort(Comparator.comparing(ExpenseResponseDTO.MonthExpenseDTO::getExpenseDate));

        return monthlyExpenditureList;
    }

    // 사용자의 목표 달성 일수 구하기
    // 사용자가 지정한 하루 목표 지출액 >= 하루 지출액 일 경우 -> 목표 달성 일수
    public Integer calculateAchieveDays(User user, List<ExpenseResponseDTO.MonthExpenseDTO> monthExpenses){
        int ahieveDays = 0;
        int dayTargetExpenditure = user.getDayTargetExpenditure();

        for (ExpenseResponseDTO.MonthExpenseDTO expense : monthExpenses) { // 지출이 있는 날의 총 지출액
            if (dayTargetExpenditure >= expense.getExpenditure()) {
                ahieveDays++;
            }
        }

        return ahieveDays;
    }
}
