package edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * defines a step in a workflow, which has an optional previous.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface Step{

    /**
     * defines the previous for this step, or is empty if there is no previous
     *
     * @return
     */
    String previous() default "None";

    /**
     * an unique name over all steps
     * @return
     */
    String name() default "None";

    /**
     * a simple description was this step does
     * @return
     */
    String description() default "";
    /**
     * optional class value for the step, if you don't want to use names.
     * @return
     */
    Class previousClass() default void.class;

}
