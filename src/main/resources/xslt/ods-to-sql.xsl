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
	<xsl:param name="default-lang" select="'en'"/>
	
	<xsl:include href="ods-common.xsl"/>
	
	<xsl:output method="text" encoding="UTF-8" name="text"/>
	
	<xsl:variable name="max-string-length" select="max(//table:table-cell/text:p/string-length(string()))"/>
	
	<xsl:template match="/">
		<xsl:result-document href="{concat($dest, '/', $name, '.sql')}" format="text">
			<xsl:text>SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";&#xA;</xsl:text>
			<xsl:text>SET time_zone = "+00:00";&#xA;</xsl:text>
			<xsl:text>SET NAMES 'utf8';&#xA;</xsl:text>
			<xsl:text>&#xA;</xsl:text>
			<xsl:text>DROP TABLE IF EXISTS `</xsl:text>
			<xsl:value-of select="$name"/>
			<xsl:text>`;&#xA;</xsl:text>
			<xsl:text>CREATE TABLE `</xsl:text>
			<xsl:value-of select="$name"/>
			<xsl:text>` (&#xA;</xsl:text>
			<xsl:text>  `id` int(3) DEFAULT NULL,&#xA;</xsl:text>
			<xsl:for-each select="('tag', $languages)">
				<xsl:text>  `</xsl:text>
				<xsl:value-of select="."/>
				<xsl:text>` varchar(</xsl:text>
				<xsl:value-of select="$max-string-length"/>
				<xsl:text>) DEFAULT NULL,&#xA;</xsl:text>
			</xsl:for-each>
			<xsl:text>  KEY `id` (`id`)&#xA;</xsl:text>
			<xsl:text>) ENGINE=MyISAM DEFAULT CHARSET=utf8;&#xA;</xsl:text>
			<xsl:text>&#xA;</xsl:text>
			<xsl:text>INSERT INTO `</xsl:text>
			<xsl:value-of select="$name"/>
			<xsl:text>` (`</xsl:text>
			<xsl:value-of select="string-join(('id', 'tag', $languages), '`, `')"/>
			<xsl:text>`) VALUES&#xA;</xsl:text>
			<xsl:variable name="rows" as="xs:string*">
				<xsl:for-each select="$spreadsheet/table:table">
					<xsl:for-each select="table:table-row[position() &gt; 1]">
						<xsl:variable name="key" select="string(table:table-cell[1]/text:p)"/>
						<xsl:if test="$key != ''">
							<xsl:sequence select='string-join((concat("&apos;", $key, "&apos;"), my:get-row-values(., count($languages))), ", ")'/>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:variable>
			<xsl:for-each select="$rows">
				<xsl:text>(</xsl:text>
				<xsl:value-of select="position()"/>
				<xsl:text>, </xsl:text>
				<xsl:value-of select='.'/>
				<xsl:text>)</xsl:text>
				<xsl:value-of select="if (position()=last()) then ';' else ','"/>
				<xsl:text>&#xA;</xsl:text>
			</xsl:for-each>
		</xsl:result-document>
	</xsl:template>

	<xsl:function name="my:get-row-values" as="xs:string*">
		<xsl:param name="table-row"/>
		<xsl:param name="columns"/>
		<xsl:for-each select="my:get-row-cells($table-row, 1, $columns)">
			<xsl:sequence select='concat("&apos;", replace(string(./text:p), "&apos;", "&apos;&apos;"), "&apos;")'/>
		</xsl:for-each>
	</xsl:function>

</xsl:stylesheet>