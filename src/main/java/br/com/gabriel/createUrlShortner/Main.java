package br.com.gabriel.createUrlShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client client = S3Client.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {

        String pathParameters = input.get("rawPath").toString();
        String shortUrlCode = pathParameters.replace("/", "");

        if(shortUrlCode == null || shortUrlCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required.");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-shortener-bucket-gabriel-script-dev")
                .key(shortUrlCode + ".json")
                .build();

        InputStream s3ObjectStream;

        try {
            s3ObjectStream = client.getObject(getObjectRequest);
        } catch(Exception e) {
            throw new RuntimeException("Error fetching data from S3: " + e.getMessage(), e);
        }

        UrlData urlData;

        try {
            urlData = mapper.readValue(s3ObjectStream, UrlData.class);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing URL Data: " + e.getMessage(), e);
        }

        long currentTimeIsSeconds = System.currentTimeMillis() / 1000;
        
        Map<String, Object> response = new HashMap<>();

        if(currentTimeIsSeconds < urlData.getExpirationTime()) {
            
            response.put("statusCode", 302);

            Map<String,String> headers = new HashMap<>();

            headers.put("Location", urlData.getOriginalUrl());
            response.put("headers", headers);

            return response;
        }

        response.put("statusCode", 410);
        response.put("body", "This URL has expired.");

        return response;
    }
}