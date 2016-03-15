package edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * used to define an element to merge several pieces of a workflow back together
 * and than children can connect to this
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
@Inherited
public @interface Continuation {

    /**
     * the name of the continuation
     * @return
     */
    String name();

    /**
     * the possible parents of this continuation
     * @return
     */
    String[] parents();
}
