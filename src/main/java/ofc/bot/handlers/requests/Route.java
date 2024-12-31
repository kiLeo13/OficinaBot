package ofc.bot.handlers.requests;

import com.google.gson.Gson;
import net.dv8tion.jda.api.utils.data.DataObject;
import ofc.bot.handlers.requests.exceptions.HttpRequestException;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static ofc.bot.handlers.requests.Method.*;

public final class Route {
    private static final Logger LOGGER = LoggerFactory.getLogger(Route.class);
    private static final OkHttpClient client = new OkHttpClient();
    private final Method method;
    private final String route;

    public static class UnbelievaBoat {
        private static final String BASE_URL = "https://unbelievaboat.com/api/v1/";

        // ---------- Economy ----------
        public static final Route GET_BALANCE    = new Route(GET,   BASE_URL + "guilds/%s/users/%s");
        public static final Route SET_BALANCE    = new Route(PUT,   BASE_URL + "guilds/%s/users/%s");
        public static final Route UPDATE_BALANCE = new Route(PATCH, BASE_URL + "guilds/%s/users/%s");
    }

    public static class MEE6 {
        public static final Route GET_LEADERBOARD = new Route(GET, "https://mee6.xyz/api/plugins/levels/leaderboard/%s");
    }

    public static class IPs {
        public static final Route GET_IP_INFO = new Route(GET, "http://ip-api.com/json/%s");
    }

    public static class Games {
        public static final Route GET_EPICSTORE_FREE_GAMES = new Route(GET, "https://free-epic-games.p.rapidapi.com/free");
    }

    public Route(Method method, String route) {
        this.method = method;
        this.route = route;
    }

    public static Route get(String route) {
        return new Route(GET, route);
    }

    public RequestBuilder create(Object... params) {
        return new RequestBuilder(this.method, String.format(this.route, params));
    }

    private static RequestMapper request(boolean warnFailure, Request req) throws HttpRequestException {
        Call call = client.newCall(req);
        try (Response resp = call.execute()) {
            ResponseBody body = resp.body();
            byte[] bytes = body == null ? new byte[0] : body.bytes();
            int code = resp.code();

            if (!resp.isSuccessful() && warnFailure) {
                String strBody = new String(bytes);
                LOGGER.warn("Request to \"{}\" was not successful, code: {}, reason: {}", req.url(), code, strBody);
            }

            return new RequestMapper(bytes, code);
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }

    public static class RequestBuilder {
        private static final Gson GSON = new Gson();
        private final Method method;
        private final HttpUrl.Builder urlBuilder;
        private final Map<String, String> headers;
        private boolean warnIfFail = true;
        private String body;
        private String contentType;

        @SuppressWarnings("DataFlowIssue")
        private RequestBuilder(Method method, String route) {
            this.method = method;
            this.urlBuilder = HttpUrl.parse(route).newBuilder();
            this.headers = new HashMap<>();
            this.contentType = "application/json";
        }

        public RequestBuilder addQueryParam(String key, Object value) {
            this.urlBuilder.addQueryParameter(key, String.valueOf(value));
            return this;
        }

        public RequestBuilder setBody(String body) {
            this.body = body.strip();
            return this;
        }

        public RequestBuilder warnIfFail(boolean flag) {
            this.warnIfFail = flag;
            return this;
        }

        public RequestBuilder setBody(DataObject body) {
            this.body = body.toString();
            return this;
        }

        public RequestBuilder addHeader(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        /**
         * This is the same as turning the provided {@code obj} into
         * a JSON and calling {@link #setBody(String)}.
         *
         * @param obj the object to be turned into JSON.
         * @return the same builder instance, for chaining convenience.
         */
        public RequestBuilder setBody(Object obj) {
            return setBody(GSON.toJson(obj));
        }

        public RequestBuilder setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public RequestMapper send() {
            Request.Builder req = new Request.Builder()
                    .url(this.urlBuilder.build());

            if (this.body != null && contentType != null)
                req.method(this.method.toString(), RequestBody.create(this.body, MediaType.parse(this.contentType)));

            if (!this.headers.isEmpty())
                headers.forEach(req::addHeader);

            return request(warnIfFail, req.build());
        }

        public <T> T send(Function<RequestMapper, T> resolver) {
            return resolver.apply(this.send());
        }

        public <T> T send(BiFunction<RequestMapper, Integer, T> resolver) {
            RequestMapper mapper = this.send();
            return resolver.apply(mapper, mapper.getStatusCode());
        }
    }
}