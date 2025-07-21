package org.hazem.sse.projectv2.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@CrossOrigin
public class NewsController {
    public List<SseEmitter> emitters = new CopyOnWriteArrayList<SseEmitter>();
    //methode for client subscribtion
    @RequestMapping(value = "/subscribe",consumes = MediaType.ALL_VALUE)
    @CrossOrigin
    @GetMapping
    public SseEmitter subscribe() {
        try {
            SseEmitter emitter = new SseEmitter();
            emitters.add(emitter);
            emitter.onCompletion(() -> emitters.remove(emitter));
            emitter.onTimeout(() -> emitters.remove(emitter));
            emitter.onError((e) -> emitters.remove(emitter));



            return emitter;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }
    @PostMapping("/publish")
    public void publish(@RequestParam("message") String message) {
        List<SseEmitter> deadEmitters = new ArrayList<>();

        emitters.forEach(emitter -> {
            try {
                emitter.send(SseEmitter.event().name("agent").data(message));
            } catch (IOException e) {
                // Client disconnected, mark emitter for removal
                deadEmitters.add(emitter);
            }
        });

        // Remove dead emitters to prevent memory leaks
        emitters.removeAll(deadEmitters);
    }

    //methode for dispatching news
}
