package restql.core.annotation.util;

import restql.core.annotation.RestEntity;
import restql.core.annotation.RestRelation;

@RestEntity(
        name = "light-saber",
        resourceMapping = "https://localhost/api/light-saber"
)
public class LightSaber {

    private Long id;

    private String color;

    @RestRelation(targetAttribute = "id", mappedBy = "light-saber.jediId")
    private Jedi jedi;
}
