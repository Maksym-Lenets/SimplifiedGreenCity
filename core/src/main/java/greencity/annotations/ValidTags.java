package greencity.annotations;

import greencity.constant.ValidationConstants;
import greencity.validator.TagsValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = TagsValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ValidTags {
    /**
     * Defines the message that will be showed when the input data is not valid.
     *
     * @return message
     */
    String message() default "Invalid tags. You must have less than "
        + ValidationConstants.MAX_AMOUNT_OF_TAGS + " tags";

    /**
     * Let you select to split the annotations into different groups
     * to apply different validations to each group.
     *
     * @return groups
     */
    Class<?>[] groups() default {};

    /**
     * Payloads are typically used to carry metadata information
     * consumed by a validation client.
     *
     * @return payload
     */
    Class<? extends Payload>[] payload() default {};
}
