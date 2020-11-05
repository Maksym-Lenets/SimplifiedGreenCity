package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatus.HabitStatusDto;
import greencity.dto.habitstatus.HabitStatusVO;
import greencity.dto.habitstatus.UpdateHabitStatusDto;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarVO;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatus;
import greencity.entity.HabitStatusCalendar;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.HabitStatusRepo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class HabitStatusServiceImpl implements HabitStatusService {
    private final HabitStatusRepo habitStatusRepo;
    private final HabitStatusCalendarService habitStatusCalendarService;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto getById(Long id) {
        return modelMapper.map(habitStatusRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_ASSIGN + id)),
            HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveStatusByHabitAssign(HabitAssignVO habitAssign) {
        HabitStatus habitStatus = HabitStatus.builder()
            .habitStreak(0)
            .habitAssign(modelMapper.map(habitAssign, HabitAssign.class))
            .workingDays(0)
            .lastEnrollmentDate(LocalDateTime.now())
            .build();
        habitStatusRepo.save(habitStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto findActiveStatusByHabitIdAndUserId(Long habitId, Long userId) {
        return modelMapper.map(habitStatusRepo.findByHabitIdAndUserId(habitId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_AND_USER + habitId)),
            HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto findStatusByHabitAssignId(Long habitAssignId) {
        return modelMapper.map(habitStatusRepo.findByHabitAssignId(habitAssignId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_ASSIGN + habitAssignId)),
            HabitStatusDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto enrollHabit(Long habitId, Long userId) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_AND_USER + habitId));
        LocalDate todayDate = LocalDate.now();

        updateHabitStatus(habitStatus, todayDate);

        HabitStatusCalendar habitCalendar = HabitStatusCalendar.builder()
            .enrollDate(todayDate).habitStatus(habitStatus).build();

        habitStatusCalendarService.save(modelMapper.map(habitCalendar, HabitStatusCalendarVO.class));
        return modelMapper.map(habitStatusRepo.save(habitStatus), HabitStatusDto.class);
    }

    /**
     * Method updates {@link HabitStatus} fields after habit enroll.
     *
     * @param habitStatus {@link HabitStatus} instance.
     * @param todayDate   {@link LocalDate} date.
     */
    private void updateHabitStatus(HabitStatus habitStatus, LocalDate todayDate) {
        int workingDays = habitStatus.getWorkingDays();
        int habitStreak = habitStatus.getHabitStreak();
        habitStatus.setWorkingDays(++workingDays);
        habitStatus.setLastEnrollmentDate(LocalDateTime.now());

        LocalDate lastEnrollmentDate = habitStatusCalendarService.findTopByEnrollDateAndHabitStatus(
            modelMapper.map(habitStatus, HabitStatusVO.class));

        long intervalBetweenDates = 0;
        if (lastEnrollmentDate != null) {
            intervalBetweenDates = Period.between(lastEnrollmentDate, todayDate).getDays();
        }
        if ((intervalBetweenDates == 1) || lastEnrollmentDate == null) {
            habitStatus.setHabitStreak(++habitStreak);
        } else if (intervalBetweenDates > 1) {
            habitStreak = 1;
            habitStatus.setHabitStreak(habitStreak);
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_HAS_BEEN_ALREADY_ENROLLED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unenrollHabit(Long habitId, Long userId, LocalDate date) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_AND_USER + habitId));
        int daysStreak = checkHabitStreakAfterDate(date, habitStatus);
        habitStatus.setHabitStreak(daysStreak + 1);
        int workingDays = habitStatus.getWorkingDays();

        if (workingDays == 0) {
            habitStatus.setWorkingDays(0);
        } else {
            habitStatus.setWorkingDays(--workingDays);
        }

        habitStatusRepo.save(habitStatus);
        HabitStatusCalendarVO habitStatusCalendarVO =
            habitStatusCalendarService
                .findHabitStatusCalendarByEnrollDateAndHabitStatus(
                    date, modelMapper.map(habitStatus, HabitStatusVO.class));
        if (habitStatusCalendarVO != null) {
            habitStatusCalendarService.delete(habitStatusCalendarVO);
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_IS_NOT_ENROLLED);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitStatusDto enrollHabitInDate(Long habitId, Long userId, LocalDate date) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_AND_USER + habitId));
        HabitStatusCalendarVO habitCalendarOnDate =
            habitStatusCalendarService.findHabitStatusCalendarByEnrollDateAndHabitStatus(date,
                modelMapper.map(habitStatus, HabitStatusVO.class));

        int daysStreakAfterDate = checkHabitStreakAfterDate(date, habitStatus);
        int daysStreakBeforeDate = checkHabitStreakBeforeDate(date, habitStatus);

        if (daysStreakBeforeDate != 0) {
            daysStreakBeforeDate += 1;
        }

        if (habitCalendarOnDate == null) {
            HabitStatusCalendar habitCalendar = HabitStatusCalendar.builder()
                .enrollDate(date).habitStatus(habitStatus).build();

            habitStatusCalendarService.save(modelMapper.map(habitCalendar, HabitStatusCalendarVO.class));

            if (Period.between(date, LocalDate.now()).getDays() == daysStreakAfterDate + 1) {
                if ((daysStreakAfterDate + daysStreakBeforeDate) == 1) {
                    habitStatus.setHabitStreak(daysStreakAfterDate + daysStreakBeforeDate + 1);
                } else {
                    habitStatus.setHabitStreak(daysStreakAfterDate + daysStreakBeforeDate + 2);
                }
            }

            habitStatus.setWorkingDays(habitStatus.getWorkingDays() + 1);
            return modelMapper.map(habitStatusRepo.save(habitStatus), HabitStatusDto.class);
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_HAS_BEEN_ALREADY_ON_THAT_DAY);
        }
    }

    private int checkHabitStreakAfterDate(LocalDate dateTime, HabitStatus habitStatus) {
        int daysStreak = 0;

        List<LocalDate> enrollDates = habitStatusCalendarService.findEnrolledDatesAfter(dateTime,
            modelMapper.map(habitStatus, HabitStatusVO.class));
        Collections.sort(enrollDates);

        for (int i = 0; i < enrollDates.size() - 1; i++) {
            if (Period.between(enrollDates.get(i), enrollDates.get(i + 1)).getDays() == 1) {
                daysStreak++;
            } else {
                daysStreak = 0;
            }
        }
        return daysStreak;
    }

    private int checkHabitStreakBeforeDate(LocalDate dateTime, HabitStatus habitStatus) {
        int daysStreak = 0;

        List<LocalDate> enrollDates = habitStatusCalendarService.findEnrolledDatesBefore(dateTime,
            modelMapper.map(habitStatus, HabitStatusVO.class));
        Collections.sort(enrollDates);
        Collections.reverse(enrollDates);

        for (int i = 0; i < enrollDates.size() - 1; i++) {
            if (Period.between(enrollDates.get(i + 1), enrollDates.get(i)).getDays() == 1) {
                daysStreak++;
            } else {
                return daysStreak;
            }
        }

        return daysStreak;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteStatusByHabitAssign(HabitAssignVO habitAssignVO) {
        HabitStatus habitStatus = habitStatusRepo.findByHabitAssignId(habitAssignVO.getId())
            .orElseThrow(
                () -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_ASSIGN + habitAssignVO.getId()));
        habitStatusCalendarService.deleteAllByHabitStatus(modelMapper.map(habitStatus, HabitStatusVO.class));
        habitStatusRepo.delete(habitStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public HabitStatusDto update(Long habitId, Long userId, UpdateHabitStatusDto dto) {
        HabitStatus updatable = habitStatusRepo.findByHabitIdAndUserId(habitId, userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.NO_STATUS_FOR_SUCH_HABIT_AND_USER + habitId));

        updatable.setHabitStreak(dto.getHabitStreak());
        updatable.setLastEnrollmentDate(dto.getLastEnrollmentDate());
        updatable.setWorkingDays(dto.getWorkingDays());

        return modelMapper.map(habitStatusRepo.save(updatable), HabitStatusDto.class);
    }
}
