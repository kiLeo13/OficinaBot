package ofc.bot.util;

import java.lang.annotation.Annotation;
import java.util.function.Function;

public class Annotations {

    public static <T, A extends Annotation> T get(Class<?> clazz, Class<A> ann, Function<A, T> mapper) {

        A annotation = clazz.getAnnotation(ann);

        if (annotation == null)
            return null;

        return mapper.apply(annotation);
    }

    public static <T, A extends Annotation> T get(Class<?> clazz, Class<A> ann, Function<A, T> mapper, T fallback) {

        T val = get(clazz, ann, mapper);

        return (val == null) ? fallback : val;
    }

    public static <T, A extends Annotation> T get(Object obj, Class<A> ann, Function<A, T> mapper) {
        return get(obj.getClass(), ann, mapper);
    }
}