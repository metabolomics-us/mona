package edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * defines a step in a workflow, which has an optional parent.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Step{

    /**
     * defines the parent for this step, or is empty if there is no parent
     *
     * @return
     */
    String parent() default "";

    /**
     * an unique name over all steps
     * @return
     */
    String name() default "";

    /**
     * optional class value for the step, if you don't want to use names.
     * @return
     */
    Class step() default void.class;

}
