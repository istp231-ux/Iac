package kr.or.nia.dpg.vpos.config.retrofit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import kr.or.nia.dpg.vpos.config.properties.api.ApiCertifiProperties;
import kr.or.nia.dpg.vpos.interceptor.retroifit.okhttp.ApiLogInterceptor;
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
    private final ApiLogInterceptor apiLogInterceptor;

    @Bean(name = "certifiRetrofit")
    public Retrofit CertifiRetrofit() {
        try {
            // 외부 인증 API는 주민등록번호/CI/DI/이름/연락처 등 민감 개인정보를 주고받으므로
            // 요청/응답 본문(BODY)과 헤더(HEADERS, API_KEY 포함)는 로그에 남기지 않는다.
            // BASIC: 메서드/URL/응답코드/소요시간만 기록(개인정보·인증키 미노출).
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message ->
                log.debug("[OkHttp] {}", message)
            );
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(apiLogInterceptor)
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
