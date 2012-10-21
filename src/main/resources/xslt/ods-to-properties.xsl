<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:my="http://github.com/bertfrees">
	
	<xsl:param name="dest" required="yes"/>
	<xsl:param name="name" required="yes"/>
	<xsl:param name="single-file" select="false()"/>
	<xsl:param name="default-lang" select="'en'"/>
	
	<xsl:include href="ods-common.xsl"/>
	
	<xsl:output method="text" encoding="UTF-8" name="text"/>
	
	<xsl:template match="/">
		<xsl:choose>
			<xsl:when test="not($single-file)">
				<!-- One properties file per language -->
				<xsl:for-each select="$languages">
					<xsl:variable name="file">
						<xsl:choose>
							<xsl:when test=".=$default-lang">
								<xsl:value-of select="concat($dest, '/', $name, '/', $name, '.properties')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="concat($dest, '/', $name, '/', $name, '_', ., '.properties')"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>
					<xsl:variable name="column" select="position()"/>
					<xsl:result-document href="{$file}" format="text">
						<xsl:for-each select="$spreadsheet/table:table">
							<xsl:for-each select="table:table-row[position() &gt; 1]">
								<xsl:variable name="key-cell" select="table:table-cell[1]"/>
								<xsl:variable name="value-cell" select="my:get-table-cell(table:table-cell[1], $column)"/>
								<xsl:if test="string($key-cell/text:p) != '' and string($value-cell/text:p) != ''">
									<xsl:value-of select="string($key-cell/text:p)"/>
									<xsl:text>=</xsl:text>
									<xsl:value-of select="my:to-ascii(string($value-cell/text:p))"/>
									<xsl:text>&#xA;</xsl:text>
								</xsl:if>
							</xsl:for-each>
						</xsl:for-each>
					</xsl:result-document>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<!-- All languages combined in a single file -->
				<xsl:result-document href="{concat($dest, '/', $name, '.properties')}" format="text" >
					<xsl:for-each select="$spreadsheet/table:table">
						<xsl:for-each select="table:table-row[position() &gt; 1]">
							<xsl:variable name="table-row" select="."/>
							<xsl:variable name="key-cell" select="table:table-cell[1]"/>
							<xsl:if test="string($key-cell/text:p) != ''">
								<xsl:for-each select="$languages">
									<xsl:variable name="value-cell" select="my:get-table-cell($table-row/table:table-cell[1], position())"/>
									<xsl:if test="string($value-cell/text:p) != ''">
										<xsl:value-of select="string($key-cell/text:p)"/>
										<xsl:text>.</xsl:text>
										<xsl:value-of select="."/>
										<xsl:text>=</xsl:text>
										<xsl:value-of select="my:to-ascii(string($value-cell/text:p))"/>
										<xsl:text>&#xA;</xsl:text>
									</xsl:if>
								</xsl:for-each>
							</xsl:if>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:result-document>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>