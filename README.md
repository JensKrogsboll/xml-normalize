# XML Normalizer

I am going to create a utility suited for normalizing arbitrary xml documents.

As an exercise I am going to create it with Kotlin and Gradle.

## Purpose
I recently worked in a team where we did regression testing of a SOAP service application
based on replaying a collection of SOAP requests.

Each request was replayed on two servers - a test server running the soon to be released
new version of the SOAP application - and a reference server running the current production version.

This strategy presented us with a number challenges - mainly how to handle/ignore differences that we 
wanted to accept - such as:

*   The sorting of lists in the response might differ if the no specific order has been enforced.
*   The ordering of namespaces might differ causing namespace prefixes to change.
*   The ordering of child nodes within a node might change.
*   The response might contain metadata nodes whose values will always differ - such as `<RequestReceivedTime>`

In some cases such differences could require your attention - but lets say that you are aware of 
them, and want to ignore them in order to focus on unintentional changes to any non-metadata.

One approach could be to use a tool like XMLUnit to build your own _CompareXML_ tool.

But we chose to build a tool that automatically executed each request and subsequently ran a unix-diff 
on the normalized responses.

The normalized responses makes it easy to further investigate the nature of any differences using
a one of the many tools for visualizing differences between two text files.

So - we needed a tool allowing us to efficiently normalize the responses.

I created such a tool in Java.

And now I have decided to re-invent an enhanced version of it in Kotlin.

Stay tuned...