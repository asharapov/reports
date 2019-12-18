package org.echosoft.framework.reports.test;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.echosoft.framework.reports.model.el.ELContext;
import org.echosoft.framework.reports.model.providers.ComparablePredicate;
import org.echosoft.framework.reports.processor.ExecutionContext;
import org.echosoft.framework.reports.test.model.Repository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Тестирование отчетов, шаблоны для которых были подготовлены в MS Excel.
 *
 * @author Anton Sharapov
 */
public class ExcelTest extends AbstractTest {

    @Test
    void test1() throws Exception {
        final List<Repository> repositories = getRepositories();
        final List<Repository> netflixRepos = getRepositories("netflix");
        final List<Repository> linkedinRepos = getRepositories("linkedin");

        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("now", new Date());
        ctx.getEnvironment().put("repositories", repositories);
        ctx.getEnvironment().put("netflix-repos", netflixRepos);
        ctx.getEnvironment().put("linkedin-repos", linkedinRepos);
        ctx.getEnvironment().put("ds", getDataSource());
        ctx.getEnvironment().put("p31", "quarkusio");
        final ReportInfo report = makeReport("excel-01", ctx);
        final ExecutionContext ectx = (ExecutionContext) ctx.getVariables().get("context");
        assertEquals(repositories.size(), ectx.history.get("s12").record);
        assertEquals(netflixRepos.size(), ectx.history.get("s22").record);
        assertEquals(linkedinRepos.size(), ectx.history.get("s24").record);
        assertEquals(repositories.stream().filter(r->"quarkusio".equals(r.owner.login)).count(), ectx.history.get("s32").record);
    }

    @Test
    void test2() throws Exception {
        final List<Repository> repositories = getRepositories();
        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("now", new Date());
        ctx.getEnvironment().put("repositories", repositories);
        final ReportInfo report = makeReport("excel-02", ctx);
        final ExecutionContext ectx = (ExecutionContext) ctx.getVariables().get("context");
        assertEquals(repositories.size(), ectx.history.get("s12").record);
        assertEquals(repositories.size(), ectx.history.get("s22").record);
    }

    @Test
    void test3() throws Exception {
        final List<Repository> repositories = getRepositories();
        final ELContext ctx = new ELContext();
        ctx.getEnvironment().put("now", new Date());
        ctx.getEnvironment().put("repositories", repositories);
        ctx.getEnvironment().put("ds1-predicate", new ComparablePredicate<Repository>() {
            public boolean evaluate(final Repository parent, final Repository current) {
                return parent.owner.login.equals(current.owner.login);
            }
        });
        final ReportInfo report = makeReport("excel-03", ctx);
        final ExecutionContext ectx = (ExecutionContext) ctx.getVariables().get("context");
        final Set<String> owners =
                repositories.stream()
                        .map(r -> r.owner.login)
                        .collect(Collectors.toSet());
        assertEquals(owners.size(), ectx.history.get("s12").record);
    }
}
