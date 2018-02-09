package restql.core.annotation;

public class RestRelationMeta {

    private Class entityClass;

    private String entityName;

    private String targetAttribute;

    private String mappedBy;

    private boolean isMultiple;

    public RestRelationMeta(Class entityClass,
                            String entityName,
                            String targetAttribute,
                            String mappedBy,
                            boolean isMultiple) {

        this.entityClass = entityClass;
        this.entityName = entityName;
        this.targetAttribute = targetAttribute;
        this.mappedBy = mappedBy;
        this.isMultiple = isMultiple;
    }

    public String getTargetAttribute() {
        return targetAttribute;
    }

    public String getMappedBy() {
        return mappedBy;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public String getEntityName() {
        return entityName;
    }
}
