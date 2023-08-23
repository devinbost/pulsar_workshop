import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.data.CqlVector;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class Product {
    private int product_id;
    private String product_name;
    private String product_description;
    private float product_price;

    public String getQuery(OpenAiService openAiService) {
        return String.format(
                "INSERT INTO openai.retail_products_embedding (product_id, product_name, product_description, product_price, embedding) VALUES (%d, '%s', '%s', %.2f, '%s');",
                product_id, escape(product_name), escape(product_description), product_price, getEmbedding(openAiService)
        );
    }
    public PreparedStatement getPreparedQuery(OpenAiService openAiService, CqlSession session) {
        var query = "INSERT INTO openai.retail_products_embedding (product_id, product_name, product_description, product_price, embedding) VALUES (? , ?, ?, ?, ?)";
        var preparedQuery = session.prepare(query);
        return preparedQuery;
    }

    public String getEmbeddingString(OpenAiService openAiService){
        var inquiryList = new ArrayList<String>();
        inquiryList.add(this.product_name + " --- " + this.product_description);
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                .input(inquiryList)
                .build();
        var embedding = "[" + openAiService.createEmbeddings(embeddingRequest).getData().get(0).getEmbedding()
                .stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
        return embedding;
    }
    public CqlVector<Float> getEmbedding(OpenAiService openAiService){
        var inquiryList = new ArrayList<String>();
        inquiryList.add(this.product_name + " --- " + this.product_description);
        EmbeddingRequest embeddingRequest = EmbeddingRequest.builder()
                .model("text-embedding-ada-002")
                .input(inquiryList)
                .build();
        var embedding = openAiService.createEmbeddings(embeddingRequest).getData().get(0).getEmbedding()
                .stream().map(Double::floatValue).collect(Collectors.toList());
        // TODO: Run vector search against table in DB
        var embeddingVector = CqlVector.newInstance(embedding);
        return embeddingVector;
    }
    // Helper function to handle single quotes in strings for CQL
    private String escape(String value) {
        if (value == null) {
            return null;
        }
        return value.replace("'", "''");
    }
}
