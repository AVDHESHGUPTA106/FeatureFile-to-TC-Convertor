package com.lex.FeatureClassification.advice;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResponseErrorHandler;

@Component
public class RestTemplateResponseErrorHandler
        implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse httpResponse)
            throws IOException {

        return (
                httpResponse.getStatusCode()==HttpStatus.NOT_FOUND
                        || httpResponse.getStatusCode()==HttpStatus.UNAUTHORIZED);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse)
            throws IOException {

        if (httpResponse.getStatusCode()==HttpStatus.NOT_FOUND)
        {


        } else if (httpResponse.getStatusCode()==HttpStatus.UNAUTHORIZED)
        {

        }
    }
}
