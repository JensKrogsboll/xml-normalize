        <xsl:apply-templates select="{listItemNodeName}">
            <xsl:sort select="." data-type="text" order="ascending" />
        </xsl:apply-templates>
