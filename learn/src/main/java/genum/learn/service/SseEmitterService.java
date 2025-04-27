package genum.learn.service;

import genum.learn.dto.ProgressEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseEmitterService {

    private final Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public SseEmitter createEmitter(String uploadId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> emitterMap.remove(uploadId));
        emitter.onTimeout(() -> emitterMap.remove(uploadId));
        emitter.onError(e -> emitterMap.remove(uploadId));

        emitterMap.put(uploadId, emitter);

        // Send initial event to establish connection
        try {
            emitter.send(SseEmitter.event()
                    .name("init")
                    .data(new ProgressEvent(uploadId, 0, "connected")));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    public void sendProgress(String uploadId, int progress) {
        SseEmitter emitter = emitterMap.get(uploadId);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("progress")
                        .data(new ProgressEvent(uploadId, progress, "in_progress")));
            } catch (IOException e) {
                emitterMap.remove(uploadId);
                emitter.completeWithError(e);
            }
        }
    }

    public void completeEmitter(String uploadId, boolean success) {
        SseEmitter emitter = emitterMap.get(uploadId);
        if (emitter != null) {
            try {
                String status = success ? "completed" : "failed";
                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data(new ProgressEvent(uploadId, 100, status)));
                emitter.complete();
            } catch (IOException e) {
                emitter.completeWithError(e);
            } finally {
                emitterMap.remove(uploadId);
            }
        }
    }
}
