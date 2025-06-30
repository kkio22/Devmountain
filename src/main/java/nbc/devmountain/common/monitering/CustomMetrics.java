package nbc.devmountain.common.monitering;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class CustomMetrics {

	// AI 요청 수 Counter
	private final Counter aiRequestCounter;

	// 채팅방 수, 메시지 수 Gauge용 AtomicInteger
	private final AtomicInteger chatroomCount = new AtomicInteger(0);
	private final AtomicInteger messageCount = new AtomicInteger(0);

	public CustomMetrics(MeterRegistry meterRegistry) {
		// 카운터 초기화
		this.aiRequestCounter = Counter.builder("ai_model_requests_total")
			.description("Total number of AI model requests")
			.register(meterRegistry);

		// 게이지 등록
		meterRegistry.gauge("chat_rooms_count", chatroomCount);
		meterRegistry.gauge("chat_messages_count", messageCount);
	}

	// AI 요청 관련
	public void incrementAiRequest() {
		aiRequestCounter.increment();
	}

	// 채팅방 관련
	public void incrementChatroomCount() {
		chatroomCount.incrementAndGet();
	}

	public void decrementChatroomCount() {
		chatroomCount.decrementAndGet();
	}

	public void resetChatroomCount() {
		chatroomCount.set(0);
	}

	// 메시지 관련
	public void incrementMessageCount() {
		messageCount.incrementAndGet();
	}

	public void resetMessageCount() {
		messageCount.set(0);
	}

}
