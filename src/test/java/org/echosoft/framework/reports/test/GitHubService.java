package org.echosoft.framework.reports.test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.echosoft.framework.reports.test.model.Issue;
import org.echosoft.framework.reports.test.model.Release;
import org.echosoft.framework.reports.test.model.Repository;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * @author Anton Sharapov
 */
public interface GitHubService {

    static GitHubService getInstance(final String user, final String password) {
        final OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (user != null && password != null) {
            clientBuilder.addInterceptor(new Interceptor() {
                private final String credentials = Credentials.basic(user, password);

                @Override
                public Response intercept(final Chain chain) throws IOException {
                    final Request request = chain.request();
                    final Request authRequest =
                            request.newBuilder()
                                    .header("Authorization", credentials)
                                    .build();
                    return chain.proceed(authRequest);
                }
            });
        }

        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(JacksonConverterFactory.create())
                .client(clientBuilder.build())
                .build();

        return retrofit.create(GitHubService.class);
    }

    @GET("users/{owner}/repos")
    CompletableFuture<List<Repository>> getUserRepositories(@Path("owner") String owner,
                                                            @Query("page") int pageNo, @Query("per_page") int pageSize);

    @GET("repos/{owner}/{repo}/issues")
    CompletableFuture<List<Issue>> getOpenIssues(@Path("owner") String owner, @Path("repo") String repo,
                                                 @Query("page") int pageNo, @Query("per_page") int pageSize);

    @GET("repos/{owner}/{repo}/releases")
    CompletableFuture<List<Release>> getReleases(@Path("owner") String owner, @Path("repo") String repo,
                                                 @Query("page") int pageNo, @Query("per_page") int pageSize);
}
