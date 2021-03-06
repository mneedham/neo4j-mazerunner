package translation;

import config.ConfigurationLoader;
import junit.framework.TestCase;
import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.ArrayList;
import java.util.List;

import static org.neo4j.graphdb.DynamicRelationshipType.withName;

public class WriterTest extends TestCase {

    @Test
    public void testWriteToHadoop() throws Exception {
        GraphDatabaseService db = setUpDb();

        // Use test configurations
        ConfigurationLoader.testPropertyAccess = true;

        Transaction tx = db.beginTx();
        List<Node> nodes = new ArrayList<>();

        int max = 30000;

        // Create nodes
        for (int i = 0; i < max; i++) {
            nodes.add(db.createNode());
            nodes.get(i).addLabel(DynamicLabel.label("Node"));
        }

        // Create PageRank test graph
        for (int i = 0; i < (max / 2) - 1; i++) {
            nodes.get(i).createRelationshipTo(nodes.get(i + (max / 2)), withName("CONNECTED_TO"));
            nodes.get(i + (max / 2)).createRelationshipTo(nodes.get(i + 1), withName("CONNECTED_TO"));
        }

        tx.success();
        tx.close();

        Writer.exportSubgraphToHDFS(db);
    }

    private static GraphDatabaseService setUpDb()
    {
        return new TestGraphDatabaseFactory().newImpermanentDatabaseBuilder().newGraphDatabase();
    }
}