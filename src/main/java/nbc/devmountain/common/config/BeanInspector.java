package nbc.devmountain.common.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class BeanInspector {
	@Autowired
	private List<ObjectMapper> mappers;
	@PostConstruct
	public void inspect() {
		log.info("등록된 ObjectMapper 개수: {}", mappers.size());
		mappers.forEach(m -> log.info("→ {}", m));
	}
}
