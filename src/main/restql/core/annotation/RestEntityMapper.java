package restql.core.annotation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RestEntityMapper {

    public static String getEntityQuery(Class<?> restEntityClass) {
        return getEntityQuery(restEntityClass, null);
    }

    public static String getEntityQuery(Class<?> restEntityClass, Map<String, Object> queryParams) {
        Map<String, RestEntityMeta> metaEntities = traverseRestEntity(restEntityClass, queryParams);
        return getEntityQuery(metaEntities);
    }

    public static String getEntityQuery(Map<String, RestEntityMeta> restEntityMetaMap) {
        StringBuilder queryBuilder = new StringBuilder("");

        for (RestEntityMeta metaEntity : restEntityMetaMap.values()) {
            queryBuilder
                    .append(metaEntity.getQuery())
                    .append("\n");
        }

        // Removing last newline
        queryBuilder
                .delete(queryBuilder.length() - 1, queryBuilder.length());

        return queryBuilder.toString();
    }

    public static Map<String, RestEntityMeta> traverseRestEntity(Class<?> restEntityClass,
                                                                 Map<String, Object> queryParams) {

        if (!restEntityClass.isAnnotationPresent(RestEntity.class)) {
            throw new RestEntityMappingException(restEntityClass.getName(), "Class must be annotated with "
                    + RestEntity.class.getName());
        }

        if (queryParams == null) {
            queryParams = new HashMap<>();
        }

        Map<String, RestEntityMeta> traversedEntities = new LinkedHashMap<>();

        RestEntityMeta traversedRootEntity = extractMetaFromEntity(restEntityClass, queryParams);
        traversedEntities.put(traversedRootEntity.getEntityClass().getName(), traversedRootEntity);

        if (traversedRootEntity.getRelationships() != null) {
            for (RestRelationMeta relationship : traversedRootEntity.getRelationships()) {
                if (!traversedEntities.containsKey(relationship.getEntityClass().getName())) {
                    queryParams.put(relationship.getEntityName(), relationship);

                    RestEntityMeta childTraversedEntity =
                            extractMetaFromEntity(relationship.getEntityClass(), queryParams);

                    traversedEntities.put(relationship.getEntityClass().getName(), childTraversedEntity);
                }
            }
        }

        return traversedEntities;
    }

    public static RestEntityMeta extractMetaFromEntity(Class<?> restEntityClass, Map<String, Object> queryParams) {
        RestEntity restEntityAnnotation = restEntityClass.getDeclaredAnnotation(RestEntity.class);

        if (!restEntityClass.isAnnotationPresent(RestEntity.class)) {
            throw new RestEntityMappingException(restEntityClass.getName(), "Class must be annotated with "
                    + RestEntity.class.getName());
        }

        StringBuilder queryBuilder = new StringBuilder("from ")
                .append(restEntityAnnotation.name());

        // TODO add with logic
        if (queryParams != null && !queryParams.isEmpty()) {
            boolean foundQueryParamForEntity = false;

            if (queryParams.containsKey(restEntityAnnotation.name())) {
                queryBuilder.append(" with ");
                foundQueryParamForEntity = true;

                RestRelationMeta relationshipMetadata = (RestRelationMeta) queryParams.get(restEntityAnnotation.name());

                queryBuilder
                        .append(relationshipMetadata.getTargetAttribute())
                        .append(" = ")
                        .append(relationshipMetadata.getMappedBy());
            }

            for (Map.Entry<String, Object> param : queryParams.entrySet()) {

                // RestRelationMeta are not simple query parameters, so they shouldn't be caught here
                if (param.getValue() instanceof RestRelationMeta) {
                    continue;
                }

                String[] paramPath = param.getKey().split("\\.");

                if (paramPath.length != 2) {
                    throw new RestEntityMappingException(
                            String.format("Error building query with [%s]. " +
                                            "QueryParams mapping should be in format entity.entityParam",
                                    param.getKey())
                    );
                }

                if (paramPath[0].equals(restEntityAnnotation.name())) {
                    if (!foundQueryParamForEntity) {
                        queryBuilder.append(" with ");
                        foundQueryParamForEntity = true;
                    } else {
                        queryBuilder.append(", ");
                    }

                    queryBuilder
                            .append(paramPath[1])
                            .append(" = \"")
                            .append(param.getValue().toString())
                            .append("\"");
                }
            }
        }

        List<RestRelationMeta> relationships = null;

        for (Field restEntityField : restEntityClass.getDeclaredFields()) {
            if (restEntityField.isAnnotationPresent(RestRelation.class)) {
                if (relationships == null) {
                    relationships = new ArrayList<>();
                }

                RestRelation restRelationAnnotation = restEntityField.getAnnotation(RestRelation.class);

                if (!restEntityField.getType().isAnnotationPresent(RestEntity.class)) {
                    throw new RestEntityMappingException(restEntityClass.getName(),
                            "Class on relation end must be annotated with " + RestEntity.class.getName());
                }

                relationships.add(new RestRelationMeta(
                        restEntityField.getType(),
                        restEntityField.getType().getDeclaredAnnotation(RestEntity.class).name(),
                        restRelationAnnotation.targetAttribute(),
                        restRelationAnnotation.mappedBy(),
                        restRelationAnnotation.isMultiple()
                ));
            }
        }

        if (restEntityAnnotation.ignoreErrors()) {
            queryBuilder.append(" ignore-errors");
        }

        return new RestEntityMeta(
                restEntityClass,
                restEntityAnnotation.name(),
                restEntityAnnotation.resourceMapping(),
                restEntityAnnotation.responseLookupPath(),
                queryBuilder.toString(),
                relationships
        );
    }
}
