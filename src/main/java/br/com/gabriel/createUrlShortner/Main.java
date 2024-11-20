package br.com.gabriel.createUrlShortner;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.Map;

public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final S3Client client = S3Client.builder().build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {

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

        if(currentTimeIsSeconds < urlData.getExpirationTime()) {

        }
        return Map.of();
    }
}