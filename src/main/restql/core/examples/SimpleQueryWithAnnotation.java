package restql.core.examples;

import restql.core.examples.model.Card;
import restql.core.manager.RestEntityManager;
import restql.core.query.QueryOptions;

import java.util.HashMap;
import java.util.Map;


public class SimpleQueryWithAnnotation {

    public static void main(String[] args) {

        QueryOptions opts = new QueryOptions();
        opts.setDebugging(true);
        opts.setGlobalTimeout(10000);
        opts.setTimeout(3000);

        RestEntityManager entityManager = new RestEntityManager(opts);

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("Card.id", 1);

        Card card = entityManager.fetchOne(Card.class, queryParams, opts);

        System.out.println(String.format("%s %s [ %s ] - %s",
                card.getManaCost(),
                card.getName(),
                card.getType(),
                card.getCardSet() != null ? card.getCardSet().getName() : ""));
    }
}
