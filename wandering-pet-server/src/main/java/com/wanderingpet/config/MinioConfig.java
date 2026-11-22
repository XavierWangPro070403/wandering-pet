package com.wanderingpet.config;

import com.wanderingpet.config.props.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * MinIO 客户端配置
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties properties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .httpClient(new OkHttpClient.Builder()
                        .connectTimeout(5, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .build())
                .build();
    }
}
