package restql.core.annotation.util;

import restql.core.annotation.RestEntity;
import restql.core.annotation.RestRelation;

@RestEntity(name = "jedi")
public class Jedi {

    private Long id;

    private String order;

    @RestRelation(targetAttribute = "id", mappedBy = "jedi.lightSaberId")
    private LightSaber lightSaber;
}
