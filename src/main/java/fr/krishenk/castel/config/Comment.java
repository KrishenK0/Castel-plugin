package fr.krishenk.castel.config;

import java.lang.annotation.*;

@Documented
@Target(value = {ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Comment {
    String BLANK = "";
    String[] value();
    boolean forParent() default false;
}
