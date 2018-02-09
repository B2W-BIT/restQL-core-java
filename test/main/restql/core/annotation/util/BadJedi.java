package restql.core.annotation.util;

import restql.core.annotation.RestEntity;
import restql.core.annotation.RestRelation;

@RestEntity(name = "bad-jedi")
public class BadJedi {

    @RestRelation(targetAttribute = "notFound", mappedBy = "brokenParameter")
    private Object brokenParameter;
}
