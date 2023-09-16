package fr.krishenk.castel.config;

import fr.krishenk.castel.libs.xseries.XSound;

import java.lang.annotation.*;

@Documented
@Target(value={ElementType.FIELD})
@Retention(value= RetentionPolicy.RUNTIME)
public @interface AdvancedMessage {
    XSound DEFAULT_SOUND = XSound.ENTITY_PARROT_IMITATE_HUSK;

    String actionbar() default "";

    String title() default "";

    String subtitle() default "";

    XSound sound() default XSound.ENTITY_PARROT_IMITATE_HUSK;
}
