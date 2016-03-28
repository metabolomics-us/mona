package edu.ucdavis.fiehnlab.mona.backend.core.workflow.annotations;

/**
 * Created by wohlgemuth on 3/22/16.
 */
public @interface Condition {

    String onSuccess() default "None";

    String onFailure() default "None";

    Class onSuccessClass() default Void.class;

    Class onFailureClass() default Void.class;
}
