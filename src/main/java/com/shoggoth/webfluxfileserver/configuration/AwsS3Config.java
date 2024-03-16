package com.shoggoth.webfluxfileserver.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {

    private SdkAsyncHttpClient asyncHttpClient() {
        return NettyNioAsyncHttpClient.builder()
                .writeTimeout(Duration.ZERO)
                .maxConcurrency(64)
                .build();

    }

    private S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .checksumValidationEnabled(false)
                .chunkedEncodingEnabled(true)
                .build();

    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider(AwsS3Properties awsS3Properties) {
        if (Strings.isBlank(awsS3Properties.getAccessKeyId())) {
            return DefaultCredentialsProvider.create();
        } else {
            return () -> AwsBasicCredentials.create(
                    awsS3Properties.getAccessKeyId(),
                    awsS3Properties.getSecretAccessKey()
            );
        }
    }

    @Bean
    public S3AsyncClient s3AsyncClient(AwsCredentialsProvider awsCredentialsProvider, AwsS3Properties awsS3Properties) {

        return S3AsyncClient.builder()
                .httpClient(asyncHttpClient())
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(awsCredentialsProvider)
                .endpointOverride(URI.create(awsS3Properties.getS3Endpoint()))
                .serviceConfiguration(s3Configuration())
                .build();
    }
}
