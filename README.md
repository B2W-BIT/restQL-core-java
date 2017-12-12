[![Build Status](https://travis-ci.org/B2W-BIT/restQL-core.svg?branch=master)](https://travis-ci.org/B2W-BIT/restQL-core)
# restQL-Core

restQL-core allows you to run restQL queries directly from JVM applications, making easy to fetch information from multiple services in the most efficient manner. e.g.:

```
from search
    with
        role = "hero"

from hero as heroList
    with
        name = search.results.name
```

You can learn more about restQL query language [here](https://github.com/B2W-BIT/restQL-server/wiki/RestQL-Query-Language) and [here](http://restql.b2w.io)

restQL-core is built upon the battle proven [HttpKit](http://www.http-kit.org/600k-concurrent-connection-http-kit.html) and [Clojure core.async](http://clojure.com/blog/2013/06/28/clojure-core-async-channels.html) to maximize performance and throughtput.

If you're using another languagem or working in a client application you may want to check  [restQL-Server](https://github.com/B2W-BIT/restQL-server).

## Getting Started

### Installation

Add restQL dependency to your project

**Maven**

```xml
<dependency>
	<groupId>com.b2wdigital</groupId>
        <artifactId>restql-core</artifactId>
       	<version>2.1.7</version>
</dependency>
```

**Gradle**

```
com.b2wdigital:restql-core:2.1.7
```

**Lein**

```
[com.b2wdigital/restql-core "2.1.7"]
```

### First query

**Java**

```java
ClassConfigRepository config = new ClassConfigRepository();
config.put("user", "http://your.api.url/users/:name");

RestQL restQL = new RestQL(config);
QueryResponse response = restql.executeQuery("from user with name = ?", "Duke Nukem");

System.out.println("The response JSON is: " + response.toString());
```

**Clojure**

```clojure
(require '[restql.core.api.restql :as restql])
(restql/execute-query :mappings { :user "http://your.api.url/users/:name" } :query "from user with name = $name" :params { :name "Duke Nukem" } )
```

In the example above restQL will call user API passing "Duke Nukem" in the name param.

### Configuration
restQL receives a configuration class with the API mappings. You can use the available configuration repositories -- `SystemPropertiesConfigRepository`, `PropertiesFileConfigRepository` or `ClassConfigRepository` -- or implement your own, using the `ConfigRepository` interface.

You can check more about endpoints configuration [here](https://github.com/B2W-BIT/restQL-core/wiki/Endpoint-configuration)

## Examples

### Simple Query

Retrieving all magic cards

```java
ClassConfigRepository config = new ClassConfigRepository();
config.put("cards", "http://api.magicthegathering.io/v1/cards");

RestQL restQL = new RestQL(config);

String query = "from cards as cardslist params type = ?";

QueryResponse response = restQL.executeQuery(query, "Artifact");

// The JSON String
String jsonString = response.toString();

// The mapped object
List<MTGCard> cards = result.getList("cardslist", MTGCard.class);
```

### Chained Query

Listing all cards and then fetching its details.

```java
ClassConfigRepository config = new ClassConfigRepository();
config.put("cards", "http://api.magicthegathering.io/v1/cards");
config.put("card", "http://api.magicthegathering.io/v1/cards/:id");

RestQL restQL = new RestQL(config);

String queryCardsAndDetails = "from cards as cardsList params type = ? \n"
                            + "from card as cardWithDetails params id = cardsList.id";

QueryResponse response = restQL.executeQuery(query, "Artifact");

// The JSON String
String jsonString = response.toString();

// The mapped object
List<MTGCard> cards = result.getList("cardWithDetails", MTGCard.class);
```

## Building From Source Code

As prerequisites to build restQL from source we have:

+ Java 8
+ Maven 3

Just clone this repo and run "mvn compile".

## License

Copyright © 2016 B2W Digital

Distributed under the MIT License.
