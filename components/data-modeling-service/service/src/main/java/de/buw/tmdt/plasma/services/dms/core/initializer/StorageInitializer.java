package de.buw.tmdt.plasma.services.dms.core.initializer;

import de.buw.tmdt.plasma.utilities.collections.MapUtilities;
import de.buw.tmdt.plasma.utilities.misc.ReflectionUtilities;
import de.buw.tmdt.plasma.utilities.misc.StringUtilities;
import de.buw.tmdt.plasma.utilities.misc.fuse.SimpleFuse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.SystemPropertyUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.annotation.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class StorageInitializer<T, ID> {

	private static final Logger logger = LoggerFactory.getLogger(StorageInitializer.class);

	private final Set<T> values = new HashSet<>();
	private final SimpleFuse initializationFuse = new SimpleFuse();

	private final Supplier<? extends Iterable<T>> storedElementProvider;
	private final Consumer<? super Iterable<T>> newElementPersister;
	private final Class<T> type;
	private final Function<? super T, ? extends ID> idMapper;
	private final BiConsumer<? super T, ? super T> mergeFunction;

	protected StorageInitializer(
			@NotNull Supplier<? extends Iterable<T>> storedElementProvider,
			@NotNull Consumer<? super Iterable<T>> newElementPersister,
			@NotNull Class<T> type,
			@NotNull Function<? super T, ? extends ID> idMapper,
			@NotNull BiConsumer<? super T, ? super T> mergeFunction
	) {
		this.storedElementProvider = storedElementProvider;
		this.newElementPersister = newElementPersister;
		this.type = type;
		this.idMapper = idMapper;
		this.mergeFunction = mergeFunction;
	}

	@PostConstruct
	public void initializeDatabase() {
		if (!initializationFuse.destroyIfSane()) {
			logger.warn("{} database initialization was invoked multiple times.", type.getSimpleName());
			return;
		}
		logger.info("Initializing database for for type: {}", type.getCanonicalName());
		final Set<T> foundValues = findInCodeBase();

		final Map<@NotNull ID, T> foundValuesMap = MapUtilities.map(foundValues, idMapper);

		//update the descriptions of all existing values
		for (T loadedValue : storedElementProvider.get()) {
			//replace value found by reflections by the attached entity
			T foundCollision = foundValuesMap.put(idMapper.apply(loadedValue), loadedValue);
			//patch description if necessary
			if (foundCollision != null) {
				mergeFunction.accept(loadedValue, foundCollision);
			} else {
				logger.warn("Found deprecated {} in data base which is not defined in the code base: {}", type.getSimpleName(), loadedValue);
			}
		}
		newElementPersister.accept(foundValuesMap.values());
	}

	@NotNull
	private Set<T> findInCodeBase() {
		Map<T, Class<?>> registrarMap = new HashMap<>();
		Map<T, RegistrationFailure> registrationFailureMap = new HashMap<>();

		Set<Class<?>> classes;
		try {
			classes = StorageInitializer.getClassOfPackage("de.buw.tmdt.plasma.services.dms.core.model");
		} catch (IOException e) {
			throw new RuntimeException("Failed to initialize data base for type: " + type.getCanonicalName(), e);
		}

		for (Class<?> currentClass : classes) {
			for (T value : ReflectionUtilities.getAnnotatedStaticValuesOfType(currentClass, type, Value.class)) {
				Class<?> previousRegistrar = registrarMap.put(value, currentClass);
				if (previousRegistrar != null) {
					registrationFailureMap.computeIfAbsent(
							value,
							a -> new RegistrationFailure(a, previousRegistrar)
					).addRegistrar(currentClass);
				}
			}
		}
		for (RegistrationFailure registrationFailure : registrationFailureMap.values()) {
			logger.warn("Action was declared multiple times: {}", registrationFailure);
		}
		this.values.addAll(registrarMap.keySet());
		return values;
	}

	@NotNull
	private static Set<Class<?>> getClassOfPackage(String basePackage) throws IOException {
		ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
		MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

		Set<Class<?>> candidates = new HashSet<>();
		String packageAsPath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(basePackage));
		String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + packageAsPath + "/**/*.class";
		for (Resource resource : resourcePatternResolver.getResources(packageSearchPath)) {
			if (resource.isReadable()) {
				MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
				String className = metadataReader.getClassMetadata().getClassName();
				try {
					candidates.add(Class.forName(className));
				} catch (ClassNotFoundException e) {
					logger.warn("Could not load class for name: {}", className);
				}
			}
		}
		return candidates;
	}

	@NotNull
	public Set<T> getValues() {
		return Collections.unmodifiableSet(values);
	}

	@SuppressWarnings("WeakerAccess - lib code")
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Value {

	}

	private final class RegistrationFailure {
		private final T value;
		private final List<Class<?>> registrars = new ArrayList<>();

		public RegistrationFailure(@NotNull T value, @NotNull Class<?> registrar) {
			this.value = value;
			this.registrars.add(registrar);
		}

		@NotNull
		public void addRegistrar(Class<?> registrar) {
			this.registrars.add(registrar);
		}

		@NotNull
		public T getValue() {
			return value;
		}

		@NotNull
		public List<Class<?>> getRegistrars() {
			return Collections.unmodifiableList(registrars);
		}

		@Override
		@SuppressWarnings("MagicCharacter")
		public String toString() {
			return "{\"@class\":\"ActionRegistrationFailure\""
			       + ", \"value\":" + value
			       + ", \"registrars\":" + StringUtilities.listToJson(registrars)
			       + '}';
		}
	}
}
