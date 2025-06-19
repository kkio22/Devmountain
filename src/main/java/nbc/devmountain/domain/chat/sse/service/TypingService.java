package nbc.devmountain.domain.chat.sse.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class TypingService {

	public SseEmitter createTypingEffect(String message,Long roomId){
		SseEmitter emitter = new SseEmitter();

		CompletableFuture.runAsync(() -> {
			try{
				String[] characters = message.split("");

				for(int i = 0; i < characters.length; i++){
					String typingData = String.format(
						"{\"type\":\"typing\",\"roomid\":\"%d\",\"text\":\"%s\",\"isComplete\":%s}",
						roomId,
						String.join("",characters).substring(0,i+1),
						i == characters.length - 1 ? "true" : "false"
					);
					emitter.send(typingData);

					Thread.sleep(150);
				}
				emitter.complete();
			}catch(IOException | InterruptedException e){
				emitter.completeWithError(e);
			}
		});
		return emitter;
	}

}
