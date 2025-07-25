package nbc.devmountain.domain.lecture.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/batches/result")
@Getter
public class JobResultController {

	@Value("${security.api.key}")
	private String apiKey;

	@GetMapping("/inflearn")
	public ResponseEntity<String> CrawlingResult(@RequestHeader("X-API-KEY") String requestKey) throws IOException {
		if(!requestKey.equals(apiKey)){
			return ResponseEntity.ok("잘못된 API KEY입니다.");
		}
		//해당 파일을 참조하는 경로 객체를 만듬
		Path path = Path.of("inflearn-batch-result.txt");
		//그 경로에 해당 파일 있는지 확인 로직
		if (!Files.exists(path)){
			return ResponseEntity.noContent().build();
		}
		//존재하면 그 경로에 있는 파일 읽어오기
		String content = Files.readString(path);
		//반환
		return ResponseEntity.ok(content);

	}

	@GetMapping("/embedding")
	public ResponseEntity<String> EmbeddingResult(@RequestHeader("X-API-KEY") String requestKey) throws IOException {
		if(!requestKey.equals(apiKey)){
			return ResponseEntity.ok("잘못된 API KEY입니다.");
		}
		//해당 파일을 참조하는 경로 객체를 만듬
		Path path = Path.of("embedding-batch-result.txt");
		//그 경로에 해당 파일 있는지 확인 로직
		if (!Files.exists(path)){
			return ResponseEntity.noContent().build();
		}
		//존재하면 그 경로에 있는 파일 읽어오기
		String content = Files.readString(path);
		//반환
		return ResponseEntity.ok(content);

	}
}
