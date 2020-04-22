package restql.core.examples.model;

import restql.core.annotation.RestEntity;

@RestEntity(
        name = "CardSet",
        resourceMapping = "http://api.magicthegathering.io/v1/sets/:setCode",
        responseLookupPath = "set",
        ignoreErrors = true
)
public class CardSet {
    private String code;

    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
