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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

/**
 * Generate an SQL script files from a OpenOffice spreadsheet (.ods) and run it on a MySQL database.
 *
 * @goal deploy
 */
public class DeployToSQLDatabaseMojo extends AbstractMojo {
	
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
	 * MySQL url (<host>/<database>).
	 *
	 * @parameter
	 * @required
	 */
	private String url;
	
	/**
	 * Server id in your settings.xml where you stored your username and password.
	 *
	 * @parameter
	 * @required
	 */
	private String settingsKey;
	
	/**
	 * The project currently being build.
	 *
	 * @parameter expression="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * The current Maven session.
	 *
	 * @parameter expression="${session}"
	 * @required
	 * @readonly
	 */
    private MavenSession session;

	/**
	 * The Maven BuildPluginManager component.
	 *
	 * @component
	 * @required
	 */
	private BuildPluginManager pluginManager;

	public void execute() throws MojoExecutionException {
		
		/* Generate SQL script */
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
			
		/* Connect and write to MySQL database */
		Plugin sqlPlugin = plugin(
			groupId("org.codehaus.mojo"),
			artifactId("sql-maven-plugin"),
			version("1.5"));
		Dependency mysqlConnector = new Dependency();  
		mysqlConnector.setGroupId("mysql");
		mysqlConnector.setArtifactId("mysql-connector-java");
		mysqlConnector.setVersion("5.1.9");
		sqlPlugin.addDependency(mysqlConnector);
		executeMojo(
			sqlPlugin,
			goal("execute"),
			configuration(
				element(name("driver"), "com.mysql.jdbc.Driver"),
				element(name("url"), "jdbc:mysql://" + url),
				element(name("settingsKey"), settingsKey),
				element(name("encoding"), "UTF-8"),
				element(name("fileset"),
					element(name("basedir"), outputDirectory.getAbsolutePath()))),
			executionEnvironment(
				project,
				session,
				pluginManager));
	}
}
