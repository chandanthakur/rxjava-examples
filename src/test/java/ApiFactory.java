import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

class ApiFactory {

	GeoNames geoNames() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
		return new Retrofit.Builder()
				.baseUrl("http://api.geonames.org")
				.addCallAdapterFactory(RxJavaCallAdapterFactory.create())
				.addConverterFactory(JacksonConverterFactory.create(objectMapper()))
				.client(client)
				.build()
				.create(GeoNames.class);
	}

	MeetupApi meetup() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
		Retrofit retrofit = new Retrofit.Builder()
				.baseUrl("https://api.meetup.com/")
				.addCallAdapterFactory(
						RxJavaCallAdapterFactory.create())
				.addConverterFactory(
						JacksonConverterFactory.create(objectMapper()))
				.client(client)
				.build();
		return retrofit.create(MeetupApi.class);
	}

	private ObjectMapper objectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.setPropertyNamingStrategy(
				PropertyNamingStrategy.SNAKE_CASE);
		objectMapper.configure(
				DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

}
