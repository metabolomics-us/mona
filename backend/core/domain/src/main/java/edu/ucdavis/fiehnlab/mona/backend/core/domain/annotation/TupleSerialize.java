package edu.ucdavis.fiehnlab.mona.backend.core.domain.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * required for custom serializations, in case a repository doesn't support
 * object for mapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TupleSerialize {
}
