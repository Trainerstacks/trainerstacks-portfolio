package com.trainerstacks.portfolio;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

/**
 * Reads where this copy of the app is running.
 * In Kubernetes, POD_NAME and NODE_NAME are injected by the Downward API
 * (see k8s/deployment-service.yaml). Locally it falls back to the hostname.
 */
@Component
public class RuntimeInfo {

    @Value("${app.version:1.0.0}")
    private String version;

    public String podName() {
        String pod = System.getenv("POD_NAME");
        if (pod != null && !pod.isBlank()) return pod;
        String host = System.getenv("HOSTNAME");
        if (host != null && !host.isBlank()) return host;
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown";
        }
    }

    public String nodeName() {
        String node = System.getenv("NODE_NAME");
        return (node != null && !node.isBlank()) ? node : "local machine";
    }

    public String version() {
        return version;
    }
}
