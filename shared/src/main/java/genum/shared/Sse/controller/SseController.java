package genum.shared.Sse.controller;

import genum.shared.Sse.service.SseEmitterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stream/sse")
public class SseController {

    private final SseEmitterService sseEmitterService;
    @GetMapping("/progress/{taskId}")
    public SseEmitter streamProgress(@PathVariable("taskId") String taskId) {
        return sseEmitterService.createEmitter(taskId);
    }
}
