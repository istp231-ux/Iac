package kr.or.nia.dpg.vpos.config.retrofit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.or.nia.dpg.vpos.config.properties.api.ApiCertifiProperties;
import kr.or.nia.dpg.vpos.web.api.certificate.RetrofitCertifiService;
import lombok.RequiredArgsConstructor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import java.util.concurrent.TimeUnit;

// TODO 추후 삭제 필요
@Configuration
@RequiredArgsConstructor
public class RetrofitConfig {

    private static final Logger log = LoggerFactory.getLogger(RetrofitConfig.class);

    private final ApiCertifiProperties certifiProperties;

    @Bean(name = "certifiRetrofit")
    public Retrofit CertifiRetrofit() {
        try {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                log.debug("[OkHttp] {}", message)
            );
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(chain -> {
                        Request original = chain.request();
                        Request request = original.newBuilder()
                                .header("API_KEY", certifiProperties.getApiKey())
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    })
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(certifiProperties.getBaseUrl())
                    .client(client)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(JacksonConverterFactory.create())
                    .build();

            log.info("[RetrofitConfig] Retrofit 클라이언트 초기화 완료 - baseUrl: {}", certifiProperties.getBaseUrl());
            return retrofit;
        } catch (Exception e) {
            log.error("[RetrofitConfig] Retrofit 클라이언트 초기화 실패", e);
            throw e;
        }
    }

    @Bean
    public RetrofitCertifiService retrofitCertifiService(@Qualifier("certifiRetrofit") Retrofit retrofit) {
        return retrofit.create(RetrofitCertifiService.class);
    }

}
