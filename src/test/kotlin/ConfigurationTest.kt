import dk.itealisten.xmlnormalize.Configuration
import dk.itealisten.xmlnormalize.Node
import dk.itealisten.xmlnormalize.XMLNormalizer
import org.junit.Assert
import org.junit.Test;
import java.io.File

public class ConfigurationTest {

    @Test
    fun basicTest() {

        // Build configuration
        val cfg: Configuration = Configuration(
            // Ignores
            arrayOf(
                Node("item_number"),
                Node("price")
            ),
            // Sorts
            arrayOf(
                Node("product")
                    .addChild(Node("catalog_item")),
                Node("catalog_item")
                    .addChild(Node("size")),
                Node("size")
                    .addChild(Node("color_swatch"))
            )
        )

        // Print it
        println(cfg)

        // Use it
        val source = File(javaClass.getResource("/some.xml").path)
        val target = File(source.parent,"some.transformed.xml")
        XMLNormalizer(cfg).transform(source, target)

        Assert.assertTrue("Target file was not created", target.exists())
    }
}
