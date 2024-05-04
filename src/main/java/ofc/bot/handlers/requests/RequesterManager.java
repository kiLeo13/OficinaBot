package ofc.bot.handlers.requests;

import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import ofc.bot.handlers.requests.exceptions.HttpRequestException;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RequesterManager {
    private static final OkHttpClient CLIENT = new OkHttpClient();

    public static Builder newCall(String url) {

        Checks.notBlank(url, "Url");

        return new Builder(url);
    }

    public static <T> T get(@NotNull String url, @NotNull Function<byte[], T> resolver) {

        Checks.notBlank(url, "Url");
        Checks.notNull(resolver, "Resolver");

        return newCall(url).get(resolver);
    }

    private static Response makeRequest(String url, Method method, Map<String, String> headers, String requestBody, String mediaType) throws IOException {

        Request.Builder builder = new Request.Builder()
                .url(url);

        if (requestBody != null && mediaType != null)
            builder.method(method.name(), RequestBody.create(requestBody, MediaType.parse(mediaType)));

        if (!headers.isEmpty())
            headers.forEach(builder::addHeader);

        Call call = CLIENT.newCall(builder.build());

        return call.execute();
    }

    public static class Builder {
        private final String url;
        private final Map<String, String> headers;
        private Method method;
        private String body;
        private String mediaType;

        private Builder(String url) {
            this.url = url;
            this.headers = new HashMap<>();
        }

        public Builder addHeader(@NotNull String name, @NotNull String value) {

            Checks.notBlank(name, "Header name");
            Checks.notBlank(value, "Header value");

            headers.put(name, value);
            return this;
        }

        public Builder setMethod(@NotNull Method method) {

            Checks.notNull(method, "Method");

            this.method = method;
            return this;
        }

        public Builder setBody(@Nullable String body) {
            this.body = body == null || body.isBlank() ? null : body;
            return this;
        }

        public Builder setBody(@Nullable DataObject body) {
            this.body = body == null ? null : body.toString();
            return this;
        }

        public Builder setMediaType(@Nullable String mediaType) {
            this.mediaType = mediaType == null || mediaType.isBlank() ? null : mediaType;
            return this;
        }

        public <T> T get(Function<byte[], T> resolver) {

            try (Response response = get()) {

                ResponseBody body = response.body();

                if (body == null)
                    return resolver.apply(new byte[0]);

                byte[] bytes = body.bytes();

                return resolver.apply(bytes);

            } catch (IOException e) {
                return resolver.apply(new byte[0]);
            }
        }

        public Response get() throws HttpRequestException {
            try {
                return get0();
            } catch (IOException e) {
                throw new HttpRequestException(e);
            }
        }

        private Response get0() throws IOException {
            return RequesterManager.makeRequest(
                    this.url,
                    this.method,
                    this.headers,
                    this.body,
                    this.mediaType
            );
        }
    }
}