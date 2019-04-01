package restql.core.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import restql.core.exception.ResponseParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by ideais on 14/12/16.
 */
public class QueryResponse {

    private final JsonNode parsed;
    private final ObjectMapper mapper;
    private final String rawString;

    public QueryResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            this.rawString = response;
            this.mapper = mapper;
            this.parsed = mapper.readTree(response);
        }
        catch(IOException e) {
            throw new ResponseParseException(e);
        }
    }

    public QueryResponse(Map response) {
        try {

            ObjectMapper mapper = new ObjectMapper();
            this.mapper = mapper;
            this.parsed = mapper.valueToTree(response);
            this.rawString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.parsed);
        } catch (JsonProcessingException e) {
            throw new ResponseParseException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String field, Class<T> clazz) {
        return this.get(new String[]{field}, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path[], Class<T> clazz) {
        if (clazz.equals(QueryItemResponse.class)) {
            return (T) new SimpleQueryItemResponse(parsed.get(path[0]));
        }
        else {
            return getClassResponseFromPath(path, clazz);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String field, Class<T> clazz) {
        return this.getList(new String[]{field}, clazz);
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getList(String path[], Class<T> clazz) {
        if (clazz.equals(QueryItemResponse.class)) {
            ArrayList<QueryItemResponse> result = new ArrayList<>();
            if (parsed.get(path[0]).isArray()) {
                Iterator<JsonNode> items = parsed.get(path[0]).elements();
                while(items.hasNext()) {
                    result.add(new SimpleQueryItemResponse(items.next()));
                }
            }
            else {
                Iterator<JsonNode> items = parsed.get(path[0]).get("result").elements();
                Iterator<JsonNode> details = parsed.get(path[0]).get("details").elements();
                while(items.hasNext()) {
                    result.add(new ArrayQueryItemResponse(details.next(), items.next()));
                }
            }
            return (List<T>) result;
        }
        else {
            return getListClassResponseFromPath(path, clazz);
        }
    }

    private <T> List<T> getListClassResponseFromPath(String path[], Class<T> clazz) {
        List<T> result = new ArrayList<>();

        try {
            if (parsed.get(path[0]).isArray()) {
                Iterator<JsonNode> items = parsed.get(path[0]).elements();

                while(items.hasNext()) {
                    // Gets each item from multiple responses
                    JsonNode nextItem = items.next().get("result");

                    for(int i=1; i<path.length; i++) {
                        nextItem = nextItem.get(path[i]);
                    }

                    Iterator<JsonNode> childItems = nextItem.elements();
                    while (childItems.hasNext()) {
                        result.add(mapper.treeToValue(childItems.next(), clazz));
                    }
                }

            } else {
                // Retrieving the result root node
                JsonNode nextItem = parsed.get(path[0]).get("result");

                for(int i=1; i<path.length; i++) {
                    nextItem = nextItem.get(path[i]);
                }

                Iterator<JsonNode> items = nextItem.elements();
                while (items.hasNext()) {
                    result.add(mapper.treeToValue(items.next(), clazz));
                }
            }
        }
        catch(JsonProcessingException | IndexOutOfBoundsException e) {
            throw new ResponseParseException(e);
        }

        return result;
    }

    private <T> T getClassResponseFromPath(String path[], Class<T> clazz) {
        try {
            JsonNode item = parsed.get(path[0]);
            JsonNode details = item.get("details");

            if (details.get("success").asBoolean()) {

                JsonNode nextItem = item.get("result");

                // Iterates over the tree to get the last element of the path
                for(int i=1; i<path.length; i++) {
                    nextItem = nextItem.get(path[i]);
                }

                return mapper.treeToValue(nextItem, clazz);
            }
            else {
                throw new ResponseParseException("Field [ " + path + " ] has failed");
            }
        } catch(JsonProcessingException | IndexOutOfBoundsException e) {
            throw new ResponseParseException(e);
        }
    }

    @Override
    public String toString() {
        return rawString;
    }
}