package restql.core.annotation;

import java.util.List;

public class RestEntityMeta {
    private String query;

    private Class entityClass;

    private String entityName;

    private String resourceMapping;

    private String responseLookupPath;

    private List<RestRelationMeta> relationships;

    public RestEntityMeta(Class entityClass, String entityName, String resourceMapping, String responseLookupPath,
                          String query, List<RestRelationMeta> relationships) {

        this.entityClass = entityClass;
        this.entityName = entityName;
        this.resourceMapping = resourceMapping;
        this.responseLookupPath = responseLookupPath;
        this.query = query;
        this.relationships = relationships;
    }

    public String getQuery() {
        return query;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getResourceMapping() {
        return resourceMapping;
    }

    public List<RestRelationMeta> getRelationships() {
        return relationships;
    }

    public String[] getResponseLookupPath() {
        return responseLookupPath.split("\\.");
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public String[] getFullQualifiedResponseLookupPath() {
        return this.entityName.concat(".").concat(this.responseLookupPath).split("\\.");
    }
}
