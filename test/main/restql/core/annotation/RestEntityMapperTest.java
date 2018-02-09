package restql.core.annotation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import restql.core.annotation.util.BadJedi;
import restql.core.annotation.util.Jedi;
import restql.core.annotation.util.LightSaber;
import restql.core.annotation.util.NonVipPerson;
import restql.core.annotation.util.Person;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class RestEntityMapperTest {

    @Test(expected = RestEntityMappingException.class)
    public void testInvalidEntityMapping() throws Exception {
        RestEntityMapper.getEntityQuery(String.class);
    }

    @Test
    public void testSimpleEntityMapping() throws Exception {
        String expectedResult = "from person";

        String parsedQuery = RestEntityMapper.getEntityQuery(Person.class);

        assertEquals(expectedResult, parsedQuery);
    }

    @Test
    public void testSimpleEntityMappingIgnoreErrors() throws Exception {
        String expectedResult = "from NonVipPerson ignore-errors";

        String parsedQuery = RestEntityMapper.getEntityQuery(NonVipPerson.class);

        assertEquals(expectedResult, parsedQuery);
    }

    @Test
    public void testSimpleEntityMappingWithQueryParams() throws Exception {
        String expectedResult = "from person with name = \"Luke\"";

        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("person.name", "Luke");

        String parsedQuery = RestEntityMapper.getEntityQuery(Person.class, queryParams);

        assertEquals(expectedResult, parsedQuery);
    }

    @Test(expected = RestEntityMappingException.class)
    public void testInvalidQueryParam() throws Exception {
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("name", "Luke");
        RestEntityMapper.getEntityQuery(Person.class, queryParams);
    }

    @Test(expected = RestEntityMappingException.class)
    public void testInvalidRelationMapping() throws Exception {
        RestEntityMapper.getEntityQuery(BadJedi.class);
    }

    @Test
    public void testNestedEntityMapping() throws Exception {
        String expectedJediResult = "from jedi\nfrom light-saber with id = jedi.lightSaberId";
        String parsedJediQuery = RestEntityMapper.getEntityQuery(Jedi.class);

        String expectedLightSaberResult = "from light-saber\nfrom jedi with id = light-saber.jediId";
        String parsedLightSaberQuery = RestEntityMapper.getEntityQuery(LightSaber.class);

        assertEquals(expectedJediResult, parsedJediQuery);
        assertEquals(expectedLightSaberResult, parsedLightSaberQuery);
    }

}
