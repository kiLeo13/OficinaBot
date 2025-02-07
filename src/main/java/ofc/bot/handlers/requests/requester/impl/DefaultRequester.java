package ofc.bot.handlers.requests.requester.impl;

import ofc.bot.handlers.requests.RequestMapper;
import ofc.bot.handlers.requests.exceptions.HttpRequestException;
import ofc.bot.handlers.requests.requester.Requester;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Supplier;

public final class DefaultRequester implements Requester {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequester.class);
    private static final OkHttpClient client = new OkHttpClient();
    private static final DefaultRequester INSTANCE = new DefaultRequester();

    private DefaultRequester() {}

    public static DefaultRequester getRequester() {
        return INSTANCE;
    }

    @NotNull
    @Override
    public RequestMapper makeRequest(@NotNull Supplier<Request> supplier) throws HttpRequestException {
        Request req = supplier.get();
        try (Response resp = client.newCall(req).execute()) {
            ResponseBody body = resp.body();
            byte[] bytes = body == null ? new byte[0] : body.bytes();
            int code = resp.code();

            if (!resp.isSuccessful()) {
                String strBody = new String(bytes);
                LOGGER.warn("Request to \"{}\" was not successful, code: {}, reason: {}", req.url(), code, strBody);
            }
            return new RequestMapper(bytes, code);
        } catch (IOException e) {
            throw new HttpRequestException(e);
        }
    }
}