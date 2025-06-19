package nbc.devmountain.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class WebConfig implements WebMvcConfigurer {


	@Bean
	@Primary
	public ObjectMapper defaultHttpMapper() {
		ObjectMapper mapper = new ObjectMapper();
		// 여기는 HTTP용 설정만 넣기
		mapper.findAndRegisterModules(); // JavaTimeModule 등 포함
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		return mapper;
	}
}
