package restql.core.annotation;

public class RestEntityMappingException extends RuntimeException {

    public RestEntityMappingException(String message) {
        super(message);
    }

    public RestEntityMappingException(String entityName, String cause) {
        super(String.format("Could not parse entity [%s]. Cause: %s", entityName, cause));
    }
}
