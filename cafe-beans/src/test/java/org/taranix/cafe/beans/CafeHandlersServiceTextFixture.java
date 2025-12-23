package org.taranix.cafe.beans;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.taranix.cafe.beans.annotations.classes.CafeService;
import org.taranix.cafe.beans.annotations.methods.CafeHandler;
import org.taranix.cafe.beans.annotations.modifiers.CafeName;

import java.util.Date;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CafeHandlersServiceTextFixture {
    static interface GenericHandler<T> {
        Object execute(T input);

        Object executeNamed(T input);
    }

    static interface GreetingsService {
        String getGreetings();
    }

    @CafeService
    static class GenericParametrizedSingletonHandlerService implements GenericHandler<Integer> {
        @CafeHandler
        @Override
        public Object execute(Integer input) {
            return input + 1;
        }

        @CafeHandler
        @CafeName("other")
        public Object executeNamed(Integer input) {
            return input + 2;
        }
    }

    @CafeService
    static class SingletonHandlerService {

        @CafeHandler
        public String generate() {
            return "String created by generate method";
        }

        @CafeName("other")
        @CafeHandler
        public String namedGenerate() {
            return "String created by namedGenerate method";
        }
    }

    @CafeService
    static class SingletonSingleParametrizedHandlerService {
        @CafeHandler
        public String generate(String prefix) {
            return prefix + "String created by generate method";
        }

        @CafeName("other")
        @CafeHandler
        public String namedGenerate(String prefix) {
            return prefix + "String created by namedGenerate method";
        }

    }

    @CafeService
    static class SingletonMultiParametrizedHandlerService {
        @CafeHandler
        public String generate(String prefix, Date date) {
            return prefix + "String created by generate method at" + date.getTime();
        }

        @CafeName("other")
        @CafeHandler
        public String namedGenerate(String prefix, Date date, Boolean booleanValue) {
            return prefix + "String created by namedGenerate method" + date.getTime() + " " + booleanValue;
        }

    }


    static class EnglishGreetingsService implements GreetingsService {

        @Override
        public String getGreetings() {
            return "Hello world";
        }
    }

    @CafeService
    static class SingletonSingleInheritedParametrizedHandlerService {
        @CafeHandler
        public String generate(GreetingsService greetingsService) {
            return greetingsService.getGreetings();
        }

        @CafeName("uppercase")
        @CafeHandler
        public String namedGenerate(GreetingsService greetingsService) {
            return greetingsService.getGreetings().toUpperCase();
        }

    }

    @CafeService
    static class SingletonSingleGenericParametrizedHandlerService {
        @CafeHandler
        public String generate(List<String> list) {
            return StringUtils.join(list, ',');
        }

        @CafeName("other")
        @CafeHandler
        public String namedGenerate(List<String> list) {
            return "namedGenerate:" + StringUtils.join(list, ",");
        }

    }
}
