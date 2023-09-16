package fr.krishenk.castel.config;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.FIELD})
@Retention(value= RetentionPolicy.RUNTIME)
public @interface Path {
    public String[] value();
}
