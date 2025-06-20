package nbc.devmountain.common.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BeanInspector implements ApplicationRunner {
	private final ApplicationContext applicationContext;
	@Override
	public void run(ApplicationArguments args) throws Exception {
		String[] beanNamesForType = applicationContext.getBeanNamesForType(ObjectMapper.class);
		for (String s : beanNamesForType) {
			System.out.println("bean 이름 = " + s);
		}
		System.out.println("BeanInspector.run");
	}

}
