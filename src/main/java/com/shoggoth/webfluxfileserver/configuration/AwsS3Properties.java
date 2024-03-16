package com.shoggoth.webfluxfileserver.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "aws")
public class AwsS3Properties {
    private String accessKeyId;
    private String secretAccessKey;
    private String region;
    private String s3BucketName;
    private String s3Endpoint;
    private int multipartMinPartSize;
}
