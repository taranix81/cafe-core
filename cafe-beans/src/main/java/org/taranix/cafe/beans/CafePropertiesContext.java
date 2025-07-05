package org.taranix.cafe.beans;

import lombok.Getter;
import org.taranix.cafe.beans.exceptions.CafePropertiesContextException;
import org.taranix.cafe.beans.repositories.Repository;
import org.taranix.cafe.beans.repositories.beans.BeanRepositoryEntry;
import org.taranix.cafe.beans.repositories.beans.BeansRepository;
import org.taranix.cafe.beans.repositories.typekeys.PropertyTypeKey;
import org.taranix.cafe.beans.repositories.typekeys.TypeKey;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;


public class CafePropertiesContext {

    public static final String BASE_PROPERTIES_FILENAME = "application";

    public static final String TEST_PROPERTIES_FILENAME = "application-test";
    public static final String PROPERTIES_EXTENSION = ".properties";
    public static final String YML_EXTENSION = ".yml";
    public static final String YAML_EXTENSION = ".yaml";

    @Getter
    private final Properties properties;

    private final ClassLoader classLoader;

    private CafePropertiesContext(Repository<TypeKey, BeanRepositoryEntry> repository, ClassLoader classLoader) {
        properties = new Properties();
        this.classLoader = classLoader;
        loadFromClassPath();
        loadFromWorkingDirectory();
        loadIntoRepository(repository);
    }

    public static CafePropertiesContext load(BeansRepository repository) {
        return load(repository, Thread.currentThread().getContextClassLoader());
    }

    public static CafePropertiesContext load(Repository<TypeKey, BeanRepositoryEntry> repository, ClassLoader classLoader) {
        return new CafePropertiesContext(repository, classLoader);
    }

    private void loadFromClassPath() {
        properties.putAll(loadProperties(getInputStreamFromClassLoader(BASE_PROPERTIES_FILENAME + PROPERTIES_EXTENSION)));
        properties.putAll(loadYaml(getInputStreamFromClassLoader(BASE_PROPERTIES_FILENAME + YAML_EXTENSION)));
        properties.putAll(loadYaml(getInputStreamFromClassLoader(BASE_PROPERTIES_FILENAME + YML_EXTENSION)));
        properties.putAll(loadProperties(getInputStreamFromClassLoader(TEST_PROPERTIES_FILENAME + PROPERTIES_EXTENSION)));
        properties.putAll(loadYaml(getInputStreamFromClassLoader(TEST_PROPERTIES_FILENAME + YAML_EXTENSION)));
        properties.putAll(loadYaml(getInputStreamFromClassLoader(TEST_PROPERTIES_FILENAME + YML_EXTENSION)));
    }

    private void loadFromWorkingDirectory() {
        properties.putAll(loadProperties(getInputStreamFromPath(BASE_PROPERTIES_FILENAME + PROPERTIES_EXTENSION)));
        properties.putAll(loadYaml(getInputStreamFromPath(BASE_PROPERTIES_FILENAME + YAML_EXTENSION)));
        properties.putAll(loadYaml(getInputStreamFromPath(BASE_PROPERTIES_FILENAME + YML_EXTENSION)));
    }

    private void loadIntoRepository(Repository<TypeKey, BeanRepositoryEntry> repository) {
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            if (entry.getValue() instanceof Collection<?>) {
                ((Collection<?>) entry.getValue()).forEach(v ->
                        repository.set(PropertyTypeKey.from(entry.getKey().toString())
                                , BeanRepositoryEntry.builder()
                                        .value(v)
                                        .build()));
            } else {
                repository.set(PropertyTypeKey.from(entry.getKey().toString())
                        , BeanRepositoryEntry.builder()
                                .value(entry.getValue())
                                .build());
            }
        }
    }

    private Properties loadProperties(InputStream is) {
        if (Objects.nonNull(is)) {
            Properties props = new Properties();
            try {
                props.load(is);
                return props;
            } catch (IOException e) {
                throw new CafePropertiesContextException(e.getMessage());
            }
        }
        return new Properties();
    }

    private InputStream getInputStreamFromClassLoader(String resource) {
        return classLoader.getResourceAsStream(resource);
    }

    private Properties loadYaml(InputStream inputStream) {
        if (inputStream != null) {
            Yaml yaml = new Yaml();
            Map<String, Object> yamlProperties = yaml.load(inputStream);
            if (yamlProperties != null) {
                Properties result = new Properties();
                result.putAll(flatten(yamlProperties));
                return result;
            }
        }
        return new Properties();
    }

    private Properties flatten(Map<String, Object> map) {
        return flatten(null, map);
    }

    private Properties flatten(String propertyNameChunk, Map<String, Object> map) {
        Properties result = new Properties();

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String propertyName = propertyNameChunk != null ? propertyNameChunk + "." + entry.getKey() : entry.getKey();
            Object value = map.get(entry.getKey());
            result.put(propertyName, value);

            if (value instanceof Map<?, ?>) {
                result.putAll(flatten(propertyName, (Map<String, Object>) value));
            }
        }
        return result;
    }

    private InputStream getInputStreamFromPath(String resource) {
        Path path = Paths.get(getCurrentPath().toString(), resource);
        if (Files.exists(path)) {
            try {
                return Files.newInputStream(path, StandardOpenOption.READ);
            } catch (IOException e) {
                throw new CafePropertiesContextException(e.getMessage());
            }
        }
        return null;
    }

    public Path getCurrentPath() {
        return Paths.get(".").toAbsolutePath();
    }

}
