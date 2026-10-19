package com.trainerstacks.portfolio;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** JSON endpoint for the demo: curl it in a loop to watch traffic spread across pods. */
@RestController
public class InfoController {

    private final RuntimeInfo runtime;

    public InfoController(RuntimeInfo runtime) {
        this.runtime = runtime;
    }

    @GetMapping("/api/info")
    public Map<String, String> info() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("app", "trainerstacks-portfolio");
        body.put("version", runtime.version());
        body.put("pod", runtime.podName());
        body.put("node", runtime.nodeName());
        body.put("time", Instant.now().toString());
        return body;
    }
}
