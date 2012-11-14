package be.docarch.maven.l10n;

import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.zip.ZipFile;

import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.plexus.util.DirectoryScanner;

import net.sf.saxon.Transform;
import net.sf.saxon.TransformerFactoryImpl;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Generate Java resource bundles and/or Maven filters (.properties) files from a OpenOffice spreadsheet (.ods)
 *
 * @goal generate-resources
 * @phase generate-resources
 */
public class GenerateLocalizationFilesMojo extends AbstractMojo {

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
	
	/**
	 * The files that should be converted to Java resource bundles.
	 *
	 * @parameter
	 */
	private String[] bundles;
	
	/**
	 * The files that should be converted to Maven filters.
	 *
	 * @parameter
	 */
	private String[] filters;

	public void execute() throws MojoExecutionException {
		
		if (!outputDirectory.exists()) {
			try {
				DirectoryScanner bundleScanner = new DirectoryScanner();
				DirectoryScanner filterScanner = new DirectoryScanner();
				bundleScanner.setBasedir(inputDirectory);
				filterScanner.setBasedir(inputDirectory);
				if (bundles == null)
					bundleScanner.setIncludes(new String[]{});
				else
					bundleScanner.setIncludes(bundles);
				if (filters == null)
					filterScanner.setIncludes(new String[]{});
				else
					filterScanner.setIncludes(filters);
				bundleScanner.scan();
				filterScanner.scan();
				Transformer transformer = new TransformerFactoryImpl().newTransformer(
					new StreamSource(getClass().getResource("/xslt/ods-to-properties.xsl").toString()));
				transformer.setParameter("dest", new File(outputDirectory, "bundles").toURI());
				for (String file : bundleScanner.getIncludedFiles()) {
					File input = new File(inputDirectory, file);
					transformer.setParameter("name", input.getName().replaceFirst("[.][^.]+$", ""));
					transformer.setParameter("single-file", false);
					ZipFile zip = new ZipFile(input.getAbsolutePath());
					InputStream content = zip.getInputStream(zip.getEntry("content.xml"));
					transformer.transform(new StreamSource(content), new StreamResult(System.out));
					zip.close(); }
				transformer.setParameter("dest", new File(outputDirectory, "filters").toURI());
				for (String file : filterScanner.getIncludedFiles()) {
					File input = new File(inputDirectory, file);
					transformer.setParameter("name", input.getName().replaceFirst("[.][^.]+$", ""));
					transformer.setParameter("single-file", true);
					ZipFile zip = new ZipFile(input.getAbsolutePath());
					InputStream content = zip.getInputStream(zip.getEntry("content.xml"));
					transformer.transform(new StreamSource(content), new StreamResult(System.out));
					zip.close(); }}
			catch (Exception e) {
				throw new MojoExecutionException("Error during conversion", e); }}
	}
}
