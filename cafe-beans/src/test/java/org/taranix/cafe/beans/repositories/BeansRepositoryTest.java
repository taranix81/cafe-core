package org.taranix.cafe.beans.repositories;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.taranix.cafe.beans.exceptions.BeansRepositoryException;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;
import org.taranix.cafe.beans.repositories.typekeys.BeanTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;

import java.util.Set;
import java.util.stream.Collectors;

class BeansRepositoryTest {

    @DisplayName("Should create instance of BeansRepository")
    @Test
    void shouldCreateRepository() {
        //when
        Repository<TypeKey, BeanRepositoryEntry> result = new BeansRepository();

        //then
        Assertions.assertNotNull(result);
    }

    @DisplayName("Should return one value from many instance of the same typekey if one is marked as primary")
    @Test
    void shouldPickupPrimaryValue() {
        //given
        Repository<TypeKey, BeanRepositoryEntry> repository = new BeansRepository();
        repository.set(BeanTypeKey.from(String.class), BeanRepositoryEntry.builder().value("I'm primary").primary(true).build());
        repository.set(BeanTypeKey.from(String.class), BeanRepositoryEntry.builder().value("I'm secondary").primary(false).build());

        //when
        String result = (String) repository.getOne(BeanTypeKey.from(String.class)).getValue();
        Set<String> all = repository.getMany(BeanTypeKey.from(String.class))
                .stream()
                .map(BeanRepositoryEntry::getValue)
                .map(String.class::cast)
                .collect(Collectors.toSet());

        //then
        Assertions.assertNotNull(result);
        Assertions.assertEquals("I'm primary", result);
        Assertions.assertEquals(2, all.size());
    }

    @DisplayName("Should throw exception when no primary value is set for many instances of same typekey")
    @Test
    void shouldThrowExceptionWhenNoPrimaryValue() {
        //given
        Repository<TypeKey, BeanRepositoryEntry> repository = new BeansRepository();
        repository.set(BeanTypeKey.from(String.class), BeanRepositoryEntry.builder().value("I'm primary").primary(false).build());
        repository.set(BeanTypeKey.from(String.class), BeanRepositoryEntry.builder().value("I'm secondary").primary(false).build());

        //when-then
        Assertions.assertThrowsExactly(BeansRepositoryException.class, () -> repository.getOne(BeanTypeKey.from(String.class)));
    }

    @DisplayName("Should throw exception when no primary value is set for many instances of same typekey")
    @Test
    void shouldThrowExceptionWhenWhenMoreThanPrimaryValueIsSet() {
        //given
        Repository<TypeKey, BeanRepositoryEntry> repository = new BeansRepository();
        repository.set(BeanTypeKey.from(String.class), BeanRepositoryEntry.builder().value("I'm primary").primary(true).build());

        //when-then
        Assertions.assertThrowsExactly(BeansRepositoryException.class, () ->
                repository.set(BeanTypeKey.from(String.class), BeanRepositoryEntry.builder()
                        .value("I'm secondary")
                        .primary(true)
                        .build()));
    }

    @DisplayName("Should throw exception when no values is set for given typekey")
    @Test
    void shouldThrowExceptionWhenWhenNoValueIsSet() {
        //given
        Repository<TypeKey, BeanRepositoryEntry> repository = new BeansRepository();

        //when-then
        Assertions.assertThrowsExactly(BeansRepositoryException.class, () -> repository.getOne(BeanTypeKey.from(String.class)));
    }

    @DisplayName("Should return positive contains for existing bean of given type")
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void shouldReturnContainForExistingBeansOfGivenTypekey(boolean isPrimary) {
        //given
        Repository<TypeKey, BeanRepositoryEntry> repository = new BeansRepository();

        //when
        repository.set(BeanTypeKey.from(String.class), BeanRepositoryEntry.builder()
                .value("Some value")
                .primary(isPrimary)
                .build());

        //then
        Assertions.assertTrue(repository.contains(BeanTypeKey.from(String.class)));

    }
}
