[![Build Status](https://travis-ci.org/B2W-BIT/restQL-core.svg?branch=master)](https://travis-ci.org/B2W-BIT/restQL-core)
# restQL-core-java

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

If you're using Clojure you may want to check [restQL-core](https://github.com/B2W-BIT/restQL-core) or [restQL-Server](https://github.com/B2W-BIT/restQL-server) if you're using another languagem or working in a client application.

## Getting Started

### Installation

Add Clojars repository and restQL dependency to your project

**Maven**

```xml
<repositories>
    <repository>
        <id>clojars.org</id>
        <url>http://clojars.org/repo</url>
    </repository>
</repositories>
...
<dependency>
	<groupId>com.b2wdigital</groupId>
        <artifactId>restql-core-java</artifactId>
       	<version>3.5.5</version>
</dependency>
```

**Gradle**

```
repositories {
    maven { 
        url "http://clojars.org/repo"
  }
}
...
compile 'com.b2wdigital:restql-core-java:3.5.5'
```

### First query

```java
ClassConfigRepository config = new ClassConfigRepository();
config.put("planets", "https://swapi.co/api/planets/:id");

RestQL restQL = new RestQL(config);
QueryResponse response = restQL.executeQuery("from planets with id = ?", 1);

System.out.println("The response JSON is: " + response.toString());
```

In the example above restQL will call StarWars planet API passing "1" as param.

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

QueryResponse response = restQL.executeQuery(queryCardsAndDetails, "Artifact");

// The JSON String
String jsonString = response.toString();

// The mapped object
List<MTGCard> cards = result.getList("cardWithDetails", MTGCard.class);
```

## Building From Source Code

As prerequisites to build restQL from source we have:

+ Java 11
+ Maven 3

Just clone this repo and run "mvn compile".

## License

Copyright © 2016 B2W Digital

Distributed under the MIT License.
