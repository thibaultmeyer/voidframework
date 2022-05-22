package com.voidframework.web.routing;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * A single route.
 *
 * @param httpMethod         HTTP method
 * @param routePattern       URL regex pattern
 * @param controllerInstance The controller instance
 * @param method             The method
 */
public record Route(HttpMethod httpMethod,
                    Pattern routePattern,
                    Object controllerInstance,
                    Method method) {
}
