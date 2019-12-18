package org.echosoft.framework.reports.test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.echosoft.framework.reports.test.model.Issue;
import org.echosoft.framework.reports.test.model.Release;
import org.echosoft.framework.reports.test.model.Repository;
import org.echosoft.framework.reports.test.model.Subject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Anton Sharapov
 */
public class DataTest extends AbstractTest {

    @Test
    void test() {
        final List<Repository> repos = getRepositories();
        assertNotNull(repos);
        assertTrue(repos.size() > 0);
        final List<Release> releases = getReleases("oracle", "helidon");
        assertNotNull(releases);
        assertTrue(releases.size() > 0);
        final List<Issue> issues = getOpenIssues("oracle", "helidon");
        assertNotNull(issues);
        assertTrue(issues.size() > 0);
    }

    @Test
    void testdb() throws Exception {
        final List<Repository> repos = getRepositories();
        final Set<Subject> owners = repos.stream().map(r -> r.owner).collect(Collectors.toSet());
        final String names = owners.stream().map(s -> "'" + s.login + "'").collect(Collectors.joining(","));

        final DataSource ds = getDataSource();
        try (Connection conn = ds.getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM subject WHERE login in (" + names + ")")) {
                    int cnt = 0;
                    while (rs.next()) {
                        final Subject s = new Subject(
                                rs.getLong("id"),
                                rs.getString("login"),
                                rs.getString("url"),
                                rs.getString("avatar_url"),
                                Subject.Type.valueOf(rs.getString("type"))
                        );
                        assertNotEquals(0, s.id);
                        assertNotNull(s.login);
                        assertNotNull(s.url);
                        assertNotNull(s.type);
                        if (!owners.contains(s)) {
                            System.out.println("s = " + s);
                            System.out.println("owners = " + owners);
                        }
                        assertTrue(owners.contains(s));
                        cnt++;
                    }
                    assertEquals(owners.size(), cnt);
                }

                final String sql = "" +
                        "SELECT s.id as subj_id, s.type as subj_type, s.login, s.url as subj_url, s.avatar_url as subj_avatar_url, \n" +
                        " l.id as lic_id, l.name as lic_name, l.url as lic_url, \n" +
                        " r.id, r.name, r.full_name, r.is_private, r.is_fork, r.url, r.description, r.created_at, r.updated_at, r.pushed_at, \n" +
                        " r.homepage, r.lang, r.size, r.stars, r.forks, r.watchers, r.open_issues, r.archived, not r.archived as active, r.disabled, r.default_branch " +
                        "FROM repository r \n" +
                        "JOIN subject s ON r.owner_id = s.id \n" +
                        "LEFT OUTER JOIN license l ON r.license_id = l.id \n" +
                        "WHERE s.login = 'asharapov' \n";
                try (ResultSet rs = stmt.executeQuery(sql)) {
                    int cnt = 0;
                    while (rs.next()) {
                        final String st = rs.getString("subj_type");
                        final boolean archived = rs.getBoolean("archived");
                        final boolean active = rs.getBoolean("active");
                        assertEquals(archived, !active);
                        cnt++;
                    }
                    assertEquals(repos.stream().filter(r -> "asharapov".equals(r.owner.login)).count(), cnt);
                }
            }
        }
    }
}
