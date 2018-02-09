package restql.core.examples.model;

import restql.core.annotation.RestEntity;
import restql.core.annotation.RestRelation;

@RestEntity(
        name = "Card",
        resourceMapping = "http://api.magicthegathering.io/v1/cards/:id",
        responseLookupPath = "card"
)
public class Card {
    private String manaCost;
    private String type;
    private String name;

    @RestRelation(
            targetAttribute = "setCode",
            mappedBy = "Card.card.set",
            isMultiple = false
    )
    private CardSet cardSet;

    public String getManaCost() {
        return manaCost;
    }

    public void setManaCost(String manaCost) {
        this.manaCost = manaCost;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardSet getCardSet() {
        return cardSet;
    }

    public void setCardSet(CardSet cardSet) {
        this.cardSet = cardSet;
    }
}
