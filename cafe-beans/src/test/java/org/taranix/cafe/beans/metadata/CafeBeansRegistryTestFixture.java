package org.taranix.cafe.beans.metadata;

import lombok.Getter;
import org.apache.commons.lang3.NotImplementedException;
import org.taranix.cafe.beans.annotations.CafeInject;
import org.taranix.cafe.beans.annotations.CafeProvider;
import org.taranix.cafe.beans.annotations.CafeService;

import java.util.List;
import java.util.Set;

class CafeBeansRegistryTestFixture {

    static class GenericUProviderAndTInjectable<T, U> {

        @CafeInject
        private T unknown;

        @CafeProvider
        public U getUnknown() {
            throw new NotImplementedException();
        }
    }

    @CafeService
    static class IntegerProviderAndStringInjectable extends GenericUProviderAndTInjectable<String, Integer> {
    }

    static class StringProvider {

        @CafeProvider
        String getString() {
            throw new NotImplementedException();
        }
    }


    static class ServiceClassProvider {

        @CafeProvider
        public ServiceClass getServiceClass() {
            return new ServiceClass();
        }
    }

    static class SetServiceClassInjectable {


        @CafeInject
        Set<ServiceClass> serviceClass;
    }

    static class ListServiceClassInjectable {
        @CafeInject
        List<ServiceClass> serviceClass;
    }

    @Getter
    static class ArrayServiceClassInjectable {

        @CafeInject
        ServiceClass[] serviceClass;
    }

    @CafeService
    static class ServiceClass {
    }

    @CafeService
    static class ServiceClassInjectable {

        @CafeInject
        ServiceClass serviceClass;
    }
}
