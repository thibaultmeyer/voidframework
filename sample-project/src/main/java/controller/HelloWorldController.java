package controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Inject;
import dev.voidframework.core.helper.Json;
import dev.voidframework.core.helper.Yaml;
import dev.voidframework.template.TemplateRenderer;
import dev.voidframework.web.bindable.WebController;
import dev.voidframework.web.http.Context;
import dev.voidframework.web.http.HttpContentType;
import dev.voidframework.web.http.Result;
import dev.voidframework.web.http.param.RequestBody;
import dev.voidframework.web.http.param.RequestPath;
import dev.voidframework.web.http.param.RequestRoute;
import dev.voidframework.web.routing.HttpMethod;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simple "Hello World" web controller.
 */
@WebController
public class HelloWorldController implements HttpContentType {

    private final TemplateRenderer templateRenderer;

    /**
     * Build a new instance.
     *
     * @param templateRenderer The template rendered instance
     */
    @Inject
    public HelloWorldController(final TemplateRenderer templateRenderer) {
        this.templateRenderer = templateRenderer;
    }

    /**
     * Display the home page.
     *
     * @param context The current context
     * @return A Result
     */
    @RequestRoute(method = HttpMethod.GET, route = "/")
    public Result showHomePage(final Context context) {
        return Result.ok(this.templateRenderer.render("home_page.ftl", context.getLocale()));
    }

    /**
     * Display another page.
     *
     * @param context The current context
     * @param number  A number
     * @return A Result
     */
    @RequestRoute(method = HttpMethod.GET, route = "/(?<number>[0-9]{1,36})")
    public Result sayHello(final Context context,
                           @RequestPath("number") final int number) {

        final Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("name", number);
        dataModel.put("remoteHostName", context.getRequest().getRemoteHostName());

        return Result.ok(this.templateRenderer.render("say_hello.ftl", context.getLocale(), dataModel));
    }

    /**
     * Retrieves the HTTP request headers in JSON.
     *
     * @param context The current context
     * @return A Result
     */
    @RequestRoute(method = HttpMethod.GET, route = "/json")
    public Result headersAsJson(final Context context) {

        return Result.ok(Json.toJson(context.getRequest().getHeaders()));
    }

    /**
     * Demo HTTP form.
     *
     * @param pojo The POJO retrieved from the body content
     * @return A Result
     */
    @RequestRoute(method = HttpMethod.POST, route = "/form")
    public Result postForm(@RequestBody final Pojo pojo) {
        return Result.ok(Yaml.toYaml(pojo).getBytes(StandardCharsets.UTF_8), TEXT_YAML);
    }

    /**
     * Simple POJO.
     */
    public static class Pojo {

        /**
         * POJO's unique identifier.
         */
        public final String id = UUID.randomUUID().toString();

        /**
         * POJO's first name.
         */
        public final String firstName;

        /**
         * POJO's last name.
         */
        public final String lastName;

        /**
         * Build a new instance.
         *
         * @param firstName The first name
         * @param lastName  The last name
         */
        @JsonCreator
        public Pojo(@JsonProperty("firstName") final String firstName,
                    @JsonProperty("lastName") final String lastName) {
            this.firstName = firstName;
            this.lastName = lastName;
        }
    }
}
