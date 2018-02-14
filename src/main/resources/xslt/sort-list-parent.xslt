<xsl:template match="{listParentNodeName}">
    <xsl:copy>
        <xsl:apply-templates select="@*|node()[not({listItemNodeNames})]"/>
        {listItemSort}
    </xsl:copy>
</xsl:template>
