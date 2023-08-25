# XML Normalizer

Status: Working beta version:-)

I am going to create a utility suited for normalizing arbitrary xml documents.

As an exercise I am going to create it with Kotlin and Gradle.

## Purpose
I recently worked in a team where we did regression testing of a SOAP service application
based on replaying a collection of SOAP requests.

Each request was replayed on two servers - a test server running the soon to be released
new version of the SOAP application - and a reference server running the current production version.

This strategy presented us with a number challenges - mainly how to handle/ignore differences that we 
wanted to accept - such as:

*   The sorting of lists in the response might differ if no specific order has been enforced.
*   The ordering of namespaces might differ causing namespace prefixes to change.
*   The ordering of child nodes within a node might change.
*   The response might contain metadata nodes whose values will always differ - such as `<RequestReceivedTime>`

In some cases such differences could require your attention - but lets say that you are aware of 
them, and want to ignore them in order to focus on unintentional changes to any non-metadata.

One approach could be to use a tool like XMLUnit to build your own _CompareXML_ tool.

But we chose to build a tool that automatically executed each request and subsequently ran a unix-diff 
on the normalized responses.

The normalized responses makes it easy to further investigate the nature of any differences using
one of the many tools for visualizing differences between two text files.

So - we needed a tool allowing us to efficiently normalize the responses.

I created such a tool in Java.

And now I have decided to re-invent an enhanced version of it in Kotlin.

## How to build
You dont need a Gradle installation - it's bundled in the project. 
Once you have cloned or downloaded the project, just open a Windows command prompt or
a bash shell. Then "cd" into the project folder and issue a `gradlew build` command:

```bash
$ cd xml-normalize

$ gradlew build
Starting a Gradle Daemon (subsequent builds will be faster)
:clean
:compileKotlin
:compileJava UP-TO-DATE
:copyMainKotlinClasses
:processResources
:classes
:jar
:assemble
:compileTestKotlin
:compileTestJava
:copyTestKotlinClasses
:processTestResources
:testClasses
:test
:check
:build

BUILD SUCCESSFUL

Total time: 13.745 secs
```

## Usage
Something in the neighborhood of:

```kotlin
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
XMLNormalizer(cfg).transform(source, target    
```

given this input:

```xml
<?xml version="1.0"?>
<catalog>
    <product product_image="cardigan.jpg" description="Cardigan Sweater">
        <catalog_item gender="Men's" age="2">
            <item_number>RRX9856</item_number>
            <price>39.95</price>
            <size description="Medium">
                <color_swatch image="red_cardigan.jpg">Red</color_swatch>
                <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
            </size>
            <size description="Large">
                <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
                <color_swatch image="red_cardigan.jpg">Red</color_swatch>
            </size>
        </catalog_item>
        <catalog_item age="5" gender="Women's">
            <item_number>QWZ5671</item_number>
            <price>42.50</price>
            <size description="Small">
                <color_swatch image="red_cardigan.jpg">Red</color_swatch>
                <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
                <color_swatch image="navy_cardigan.jpg">Navy</color_swatch>
            </size>
            <size description="Medium">
                <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
                <color_swatch image="red_cardigan.jpg">Red</color_swatch>
                <color_swatch image="navy_cardigan.jpg">Navy</color_swatch>
                <color_swatch image="black_cardigan.jpg">Black</color_swatch>
            </size>
            <size description="Large">
                <color_swatch image="navy_cardigan.jpg">Navy</color_swatch>
                <color_swatch image="black_cardigan.jpg">Black</color_swatch>
            </size>
            <size description="Extra Large">
                <color_swatch image="black_cardigan.jpg">Black</color_swatch>
                <color_swatch image="burgundy_cardigan.jpg">Burgundy</color_swatch>
            </size>
        </catalog_item>
    </product>
</catalog>
```

Check out the file `src/test/java/JavaTest.java` to see how to use it in a Java context.

## Java Only
I have added branch java-only with a Java only version of the project.

Stay tuned...

