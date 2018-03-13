import dk.itealisten.xmlnormalize.Configuration;
import dk.itealisten.xmlnormalize.Node;
import dk.itealisten.xmlnormalize.XMLNormalizer;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class JavaTest {

    @Test
    public void testUsingItInJava() throws Exception {

        Configuration cfg = new Configuration(
            new Node[] {
                new Node("item_number"),
                new Node("price")
            },
            new Node[] {
                new Node("product")
                    .addChild(new Node("catalog_item")),
                new Node("catalog_item")
                    .addChild(new Node("size")),
                new Node("size")
                    .addChild(new Node("color_swatch"))
            }
        );

        // Print it
        System.out.println(cfg);

        // Use it
        final File source = new File(JavaTest.class.getResource("/some.xml").getFile());
        final File target = new File(source.getParentFile(),"some.transformed.java.xml");
        new XMLNormalizer(cfg).transform(source, target);

        Assert.assertTrue("Target file was not created", target.exists());

    }
}

