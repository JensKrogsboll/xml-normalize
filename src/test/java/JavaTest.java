import dk.itealisten.xmlnormalize.Configuration;
import dk.itealisten.xmlnormalize.Node;
import dk.itealisten.xmlnormalize.XMLNormalizer;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

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
        final File expected = new File(JavaTest.class.getResource("/some.transformed.expected.xml").getFile());
        final File target = new File(source.getParentFile(), "some.transformed.xml");

        new XMLNormalizer(cfg).transform(source, target);
        Assert.assertTrue("Target file was not created", target.exists());

        String expectedText = new String(Files.readAllBytes(expected.toPath()), StandardCharsets.UTF_8);
        String targetText = new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8);
        Assert.assertEquals("The transformation target content differs from the reference:", expectedText, targetText);

    }
}

