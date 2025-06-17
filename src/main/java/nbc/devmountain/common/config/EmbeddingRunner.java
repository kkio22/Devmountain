package nbc.devmountain.common.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmbeddingRunner implements ApplicationRunner {

	private final JobLauncher jobLauncher;
	private final Job embeddingJob;

	@Override
	public void run(ApplicationArguments args) throws Exception {
		JobParameters parameters = new JobParametersBuilder()
			.addString("full", "true")
			.addLong("time", System.currentTimeMillis())
			.toJobParameters();

		jobLauncher.run(embeddingJob, parameters);
	}
}
