package org.echosoft.framework.reports.test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.poi.ss.usermodel.Workbook;
import org.echosoft.framework.reports.model.Report;
import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.parser.ReportExtension;
import org.echosoft.framework.reports.parser.ReportModelParser;
import org.echosoft.framework.reports.registry.ReportsRegistry;
import org.echosoft.framework.reports.test.model.Issue;
import org.echosoft.framework.reports.test.model.License;
import org.echosoft.framework.reports.test.model.Release;
import org.echosoft.framework.reports.test.model.Repository;
import org.echosoft.framework.reports.test.model.Subject;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Anton Sharapov
 */
public abstract class AbstractTest {

    private static final int MAX_PAGE_SIZE = 100;
    private static final Comparator<Repository> REPO_COMPARATOR =
            Comparator.comparing(Repository::getOwnerType)
                    .thenComparing(Repository::getOwnerName)
                    .thenComparing(Repository::getLang)
                    .thenComparingInt(Repository::getStars)
                    .thenComparing(Repository::getName);

    private static final ConcurrentMap<String, List<Repository>> repositories = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, List<Issue>> issues = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, List<Release>> releases = new ConcurrentHashMap<>();
    private static GitHubService service;
    private static JdbcDataSource ds;
    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll() throws IOException {
        final Path path = Paths.get("target/reports");
        Files.createDirectories(path);
        final Properties env = new Properties();
        final Path authFile = Paths.get("github.properties");
        if (Files.isRegularFile(authFile)) {
            try (Reader in = Files.newBufferedReader(authFile, StandardCharsets.UTF_8)) {
                env.load(in);
            }
        }
        String user = System.getProperty("github.user", env.getProperty("github.user", env.getProperty("user")));
        String password = System.getProperty("github.password", env.getProperty("github.token", env.getProperty("github.password")));
        service = GitHubService.getInstance(user, password);

        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("github");
            assertNotNull(emf);
        }

        if (ds == null) {
            ds = new JdbcDataSource();
            ds.setUrl("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("");
        }
    }

    DataSource getDataSource() {
        return ds;
    }

    List<Repository> getRepositories() {
        return getRepositories(
//                "netflix",
//                "linkedin",
                "quarkusio",
                "oracle",
                "ilantukh",
                "asharapov"
        );
    }

    List<Repository> getRepositories(final String... owners) {
        final List<Repository> result = new ArrayList<>();
        for (String owner : owners) {
            final List<Repository> repos = repositories.computeIfAbsent(owner, this::loadOwnerRepositories);
            result.addAll(repos);
        }
        result.sort(REPO_COMPARATOR);
        return result;
    }

    List<Issue> getOpenIssues(final String owner, final String repo) {
        final String key = owner + "/" + repo;
        return issues.computeIfAbsent(key, k -> {
            try {
                return service.getOpenIssues(owner, repo, 1, MAX_PAGE_SIZE).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    List<Release> getReleases(final String owner, final String repo) {
        final String key = owner + "/" + repo;
        return releases.computeIfAbsent(key, k -> {
            try {
                return service.getReleases(owner, repo, 1, MAX_PAGE_SIZE).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private List<Repository> loadOwnerRepositories(final String owner) {
        try {
            final List<Repository> result = new ArrayList<>();
            int pageNo = 1;
            while (true) {
                final List<Repository> repos = service.getUserRepositories(owner, pageNo++, MAX_PAGE_SIZE).get();
                result.addAll(repos);
                if (repos.size() < MAX_PAGE_SIZE) {
                    break;
                }
            }
            result.removeIf(r -> r.lang == null || r.lang.isEmpty());

            if (result.size() > 0) {
                final Subject s = result.get(0).owner;
                final EntityManager em = emf.createEntityManager();
                final EntityTransaction t = em.getTransaction();
                t.begin();
                em.persist(s);
                for (Repository r : result) {
                    if (r.license != null) {
                        final License lic = em.find(License.class, r.license.key);
                        if (lic == null) {
                            em.persist(r.license);
                        } else {
                            r.license = lic;
                        }
                    }
                    r.owner = s;
                    em.persist(r);
                }
                t.commit();
                em.close();
            }

            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Report loadReport(final String name) throws Exception {
        final String path = "org/echosoft/framework/reports/test/";
        final ClassLoader cl = GitHubService.class.getClassLoader();
        URL xmlUrl = cl.getResource(path + name + ".xml");
        if (xmlUrl == null) {
            Assertions.fail("Report '" + name + "' not found");
        }
        URL xlsUrl = cl.getResource(path + name + ".xlsx");
        if (xlsUrl == null) {
            xlsUrl = cl.getResource(path + name + ".xls");
            if (xlsUrl == null) {
                Assertions.fail("Report '" + name + "' not found");
            }
        }
        try (InputStream xlsInput = xlsUrl.openStream()) {
            try (InputStream xmlInput = xmlUrl.openStream()) {
                final List<ReportExtension> extensions = new ArrayList<>();
                return ReportModelParser.parse(xlsInput, xmlInput, extensions);
            }
        }
    }

    ReportInfo makeReport(final String name, final ELContext ctx) throws Exception {
        final Report report = loadReport(name);
        final Workbook wb = ReportsRegistry.getDefaultProcessor().process(report, ctx);
        final Path path = Paths.get("target/reports/result-" + report.getId() + "." + report.getTarget().extension);
        try (OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
            wb.write(out);
        }
        return new ReportInfo(report, wb, path);
    }


    public static class ReportInfo {
        public final Report report;
        public final Workbook wb;
        public final Path path;

        private ReportInfo(final Report report, final Workbook wb, final Path path) {
            this.report = report;
            this.wb = wb;
            this.path = path;
        }

        @Override
        public String toString() {
            return report.toString();
        }
    }
}
