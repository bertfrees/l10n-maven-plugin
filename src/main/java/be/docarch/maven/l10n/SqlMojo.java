package be.docarch.maven.l10n;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.zip.ZipFile;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Transform;
import net.sf.saxon.TransformerFactoryImpl;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generate an SQL script files from a OpenOffice spreadsheet (.ods).
 *
 * @goal sql
 */
public class SqlMojo extends AbstractMojo {
	
	/**
	 * This is where ods files are.
	 *
	 * @parameter default-value="${basedir}/src/main/l10n"
	 * @required
	 * @readonly
	 */
	private File inputDirectory;
	
	/**
	 * This is where build results go.
	 *
	 * @parameter default-value="${project.build.directory}/l10n"
	 * @required
	 * @readonly
	 */
	private File outputDirectory;

	public void execute() throws MojoExecutionException {
		
		try {
			Transformer transformer = new TransformerFactoryImpl().newTransformer(
				new StreamSource(getClass().getResource("/xslt/ods-to-sql.xsl").toString()));
			transformer.setParameter("dest", new File(outputDirectory, "sql").toURI());
			for (File input : inputDirectory.listFiles(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".ods"); }})) {
				transformer.setParameter("name", input.getName().replaceFirst("[.][^.]+$", ""));
				ZipFile zip = new ZipFile(input.getAbsolutePath());
				InputStream content = zip.getInputStream(zip.getEntry("content.xml"));
				transformer.transform(new StreamSource(content), new StreamResult(System.out));
				zip.close(); }}
		catch (Exception e) {
			throw new MojoExecutionException("Error during conversion", e); }
	}
}
