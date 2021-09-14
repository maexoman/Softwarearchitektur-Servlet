import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class SomePostThingy implements Surflet {
    @Override
    public void handleRequest(HttpRequest request, HttpResponse response) throws Exception {

        if (request.getBody().isEmpty()) {
            response.status(422).send();
            return;
        }

        JsonElement input = request.getBody().asJson();

        String name = input.getAsJsonObject().get("name").getAsString();

        JsonObject result = new JsonObject();
        result.add("id", new JsonPrimitive("someId"));
        result.add("name", new JsonPrimitive(name));

        response.status(201).json(result);




    }
}
