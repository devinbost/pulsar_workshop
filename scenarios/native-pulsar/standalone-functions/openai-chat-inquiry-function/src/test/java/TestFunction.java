import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.datastax.oss.driver.api.core.CqlSession;
import com.example.pulsarworkshop.DeviceTS;
import com.example.pulsarworkshop.Inquiry;
import com.example.pulsarworkshop.InquiryTransformFunction;
import com.example.pulsarworkshop.Order;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.theokanning.openai.service.OpenAiService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.pulsar.functions.api.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class TestFunction {
    private List<Order> collection;
    private String dbClientId;
    private String dbClientSecret;
    private String openAiToken;
    public String loadJsonFromFile(String filename) {
        // Getting the resource as a stream
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new IllegalArgumentException("File not found! " + filename);
        }

        // Reading the stream and converting it into a String
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read the file", e);
        }
    }
    public void setupDB() throws JsonProcessingException {
        // run once
        try (InputStream input = InquiryTransformFunction.class.getResourceAsStream("/config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            this.openAiToken = prop.getProperty("OPEN_AI_TOKEN");
            this.dbClientSecret = prop.getProperty("DB_CLIENT_SECRET");
            this.dbClientId = prop.getProperty("DB_CLIENT_ID");
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(0);
        }
        var astraDbSession = CqlSession.builder()
                .withCloudSecureConnectBundle(InquiryTransformFunction.class.getResourceAsStream("/secure-connect-demo.zip"))
                .withAuthCredentials(this.dbClientId,this.dbClientSecret)
                .withKeyspace("openai")
                .build();

        /*astraDbSession.execute("CREATE TABLE openai.retail_products_embedding (product_id INT PRIMARY KEY, "
                + "product_name TEXT, product_description TEXT, product_price float, embedding vector<float, 1536>);");
        astraDbSession.execute("CREATE CUSTOM INDEX retail_product_ann_index ON openai.retail_products_embedding (embedding) USING 'StorageAttachedIndex';");
*/
        var openAiService = new OpenAiService(this.openAiToken);

        String jsonData = this.loadJsonFromFile("data.json");
        var mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        var myObjects = Arrays.asList(mapper.readValue(jsonData, Product[].class));
        myObjects.stream().forEach(prod ->
                {
                    var query = prod.getPreparedQuery(openAiService, astraDbSession);
                    var boundStatement = query.bind(prod.getProduct_id(), prod.getProduct_name(), prod.getProduct_description(), prod.getProduct_price(), prod.getEmbedding(openAiService));
                    var outputs = astraDbSession.execute(boundStatement).all();
                });
        astraDbSession.close();
        openAiService.shutdownExecutor();
    }

    @Test
    public void testOpenAI() throws Exception {
        var func = new InquiryTransformFunction();
        //setupDB();

        var contextMock = mock(Context.class);
        var loggerMock = mock(Logger.class);
        when(contextMock.getLogger()).thenReturn(loggerMock);
        func.initialize(contextMock);
        String inquiryJson = "{\n" +
                "  \"customer_id\": 12345,\n" +
                "  \"inquiry_text\": \"What is the status of my order? " +
                    "prior_purchases: [" +
                        "\\\"Red Moroccan Rug, 9x12, geometrical . . . \\\"," +
                        "\\\"Red Bokhara Rug, 7 x 10, maroon pattern . . \\\"," +
                        ". . ." +
                    "]\"\n" +
                "}";
        var newOrder = func.processLogic(inquiryJson, contextMock);

    }
}
