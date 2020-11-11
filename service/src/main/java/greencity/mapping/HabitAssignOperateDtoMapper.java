package greencity.mapping;

import greencity.dto.habit.HabitAssignManagementDto;
import greencity.entity.HabitAssign;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link HabitAssign} into
 * {@link HabitAssignManagementDto}.
 */
@Component
public class HabitAssignOperateDtoMapper extends
    AbstractConverter<HabitAssign, HabitAssignManagementDto> {
    /**
     * Method convert {@link HabitAssign} to {@link HabitAssignManagementDto}.
     *
     * @return {@link HabitAssignManagementDto}
     */
    @Override
    protected HabitAssignManagementDto convert(HabitAssign habitAssign) {
        return HabitAssignManagementDto.builder()
            .id(habitAssign.getId())
            .acquired(habitAssign.getAcquired())
            .suspended(habitAssign.getSuspended())
            .createDateTime(habitAssign.getCreateDate())
            .userId(habitAssign.getUser().getId())
            .habitId(habitAssign.getHabit().getId())
            .duration(habitAssign.getDuration())
            .build();
    }
}