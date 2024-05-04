package ofc.bot.handlers.requests.exceptions;

import java.io.IOException;

public class HttpRequestException extends RuntimeException {

    public HttpRequestException(IOException e) {
        super(e);
    }
}