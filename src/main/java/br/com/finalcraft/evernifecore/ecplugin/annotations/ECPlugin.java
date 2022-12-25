package br.com.finalcraft.evernifecore.ecplugin.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ECPlugin {

    String spigotID() default "";

    String bstatsID() default "";

    //Method for the RELOAD method under an ECPlugin
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD})
    public static @interface Reload {

        //After the reload of one of these plugins, this ECPlguin will be reloaded
        String[] reloadAfter() default {};

    }

    //Method for the Logger Instantiation under an ECPlugin
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD})
    public static @interface Logger {

    }

}
