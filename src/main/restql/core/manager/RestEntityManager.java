package restql.core.manager;

import restql.core.RestQL;
import restql.core.annotation.RestEntityMapper;
import restql.core.annotation.RestEntityMeta;
import restql.core.annotation.RestRelation;
import restql.core.config.ClassConfigRepository;
import restql.core.config.ConfigRepository;
import restql.core.query.QueryOptions;
import restql.core.response.QueryResponse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestEntityManager {

    private static final Logger logger = Logger.getLogger(RestEntityManager.class.getName());

    private RestQL restQL;
    private ConfigRepository configRepository;

    public RestEntityManager() {
        this.configRepository = new ClassConfigRepository();
        this.restQL = new RestQL(null, null);
    }

    public RestEntityManager(ConfigRepository configRepository) {
        this(configRepository, null);
    }

    public RestEntityManager(QueryOptions queryOptions) {
        this(null, queryOptions);
    }

    public RestEntityManager(ConfigRepository configRepository, QueryOptions queryOptions) {
        this.configRepository = configRepository;
        this.restQL = new RestQL(configRepository, queryOptions);
    }

    public <T> QueryResponse executeQuery(Class<T> restEntityClass) {
        return this.executeQuery(restEntityClass, null, null);
    }

    public <T> QueryResponse executeQuery(Class<T> restEntityClass, QueryOptions queryOptions) {
        return this.executeQuery(restEntityClass, null, queryOptions);
    }

    public <T> QueryResponse executeQuery(Class<T> restEntityClass, Map<String, Object> queryParams, QueryOptions queryOptions) {
        // Let's retrieve all rest entity meta information first
        Map<String, RestEntityMeta> restEntityMetaMap = RestEntityMapper.traverseRestEntity(restEntityClass, queryParams);

        // Now we populate our mappings config class using the default configRepository and the class mappings
        ClassConfigRepository queryConfigRespository = new ClassConfigRepository();

        if (this.configRepository != null) {
            logger.warning("Overriding default configured mappings. Consider using only entity mappings or remove them.");

            for (Map.Entry<String, String> configMappings : this.configRepository.getMappings().toMap().entrySet()) {
                queryConfigRespository.put(configMappings.getKey(), configMappings.getValue());
            }
        }

        for (RestEntityMeta restEntityMeta : restEntityMetaMap.values()) {
            if (restEntityMeta.getResourceMapping() != null) {
                queryConfigRespository.put(restEntityMeta.getEntityName(), restEntityMeta.getResourceMapping());
            }
        }

        String query = RestEntityMapper.getEntityQuery(restEntityMetaMap);
        QueryResponse queryResponse = restQL.executeQuery(query, queryConfigRespository.getMappings(), queryOptions);

        return queryResponse;
    }

    public <T> List<T> fetch(Class<T> restEntityClass) {
        return this.fetch(restEntityClass, null, null);
    }

    public <T> List<T> fetch(Class<T> restEntityClass, Map<String, Object> queryParams) {
        return this.fetch(restEntityClass, queryParams, null);
    }

    public <T> List<T> fetch(Class<T> restEntityClass, QueryOptions queryOptions) {
        return this.fetch(restEntityClass, null, queryOptions);
    }

    public <T> List<T> fetch(Class<T> restEntityClass, Map<String, Object> queryParams, QueryOptions queryOptions) {
        QueryResponse queryResponse = this.executeQuery(restEntityClass, queryParams, queryOptions);
        RestEntityMeta restEntityMeta = RestEntityMapper.extractMetaFromEntity(restEntityClass, queryParams);

        List<T> queryResponseEntityList = queryResponse.getList(restEntityMeta.getFullQualifiedResponseLookupPath(), restEntityClass);
        for (int i = 0; i < queryResponseEntityList.size(); i++) {
            T queryResponseEntity = queryResponseEntityList.get(i);
            resolveRelationshipsForEntity(queryResponse, queryResponseEntity, restEntityMeta);
        }

        return queryResponseEntityList;
    }

    public <T> T fetchOne(Class<T> restEntityClass) {
        return this.fetchOne(restEntityClass);
    }

    public <T> T fetchOne(Class<T> restEntityClass, Map<String, Object> queryParams) {
        return this.fetchOne(restEntityClass, queryParams, null);
    }

    public <T> T fetchOne(Class<T> restEntityClass, QueryOptions queryOptions) {
        return this.fetchOne(restEntityClass, null, queryOptions);
    }

    public <T> T fetchOne(Class<T> restEntityClass, Map<String, Object> queryParams, QueryOptions queryOptions) {
        QueryResponse queryResponse = this.executeQuery(restEntityClass, queryParams, queryOptions);
        RestEntityMeta restEntityMeta = RestEntityMapper.extractMetaFromEntity(restEntityClass, queryParams);

        T queryResponseEntity = queryResponse.get(restEntityMeta.getFullQualifiedResponseLookupPath(), restEntityClass);
        resolveRelationshipsForEntity(queryResponse, queryResponseEntity, restEntityMeta);

        return queryResponseEntity;
    }

    protected <T> void resolveRelationshipsForEntity(QueryResponse queryResponse,
                                                     T entity,
                                                     RestEntityMeta restEntityMeta) {

        for (Field field : restEntityMeta.getEntityClass().getDeclaredFields()) {
            // Non relation, just skip
            if (!field.isAnnotationPresent(RestRelation.class)) {
                continue;
            }

            // Trying to find the setter for the RestRelation annotated class
            String fieldSetterName = "set"
                    .concat(field.getName().substring(0, 1).toUpperCase())
                    .concat(field.getName().substring(1, field.getName().length()));

            Method setterMethod = null;
            try {
                setterMethod = entity.getClass().getMethod(fieldSetterName, field.getType());

                Class<?> restRelationEntityClass = field.getType();
                RestRelation restRelationAnnotation = field.getDeclaredAnnotation(RestRelation.class);
                RestEntityMeta restRelationEntityMeta = RestEntityMapper.extractMetaFromEntity(restRelationEntityClass,
                        null);

                // If it's a list, we just parse as a list
                if (restRelationAnnotation.isMultiple()) {
                    setterMethod.invoke(
                            entity,
                            queryResponse.getList(
                                    restRelationEntityMeta.getFullQualifiedResponseLookupPath(),
                                    restRelationEntityClass
                            )
                    );
                } else {
                    setterMethod.setAccessible(true);
                    setterMethod.invoke(
                            entity,
                            restRelationEntityClass.cast(queryResponse.get(
                                    restRelationEntityMeta.getFullQualifiedResponseLookupPath(),
                                    restRelationEntityClass)
                            )
                    );
                }
            } catch (Exception ex) {
                // Setter method not found, just skip
                logger.info("Error calling setter for field " + field.getName());
                throw new RuntimeException("Error calling setter");
            }
        }
    }
}
