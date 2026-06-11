package com.sentinelasm.scanner;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "scanner")
public class ScannerProperties {
    private String mode = "mock";
    private String projectRoot = "..";
    private String workspace;
    private String outputRoot = "output";
    private long timeoutSeconds = 600;
    private Tools tools = new Tools();

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getProjectRoot() { return projectRoot; }
    public void setProjectRoot(String projectRoot) { this.projectRoot = projectRoot; }
    public String getWorkspace() { return workspace; }
    public void setWorkspace(String workspace) { this.workspace = workspace; }
    public String getOutputRoot() { return outputRoot; }
    public void setOutputRoot(String outputRoot) { this.outputRoot = outputRoot; }
    public long getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(long timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
    public Tools getTools() { return tools; }
    public void setTools(Tools tools) { this.tools = tools; }

    public static class Tools {
        private String submerge;
        private String httpx;
        private String nmap;
        private String whatweb;
        private String nuclei;

        public String getSubmerge() { return submerge; }
        public void setSubmerge(String submerge) { this.submerge = submerge; }
        public String getHttpx() { return httpx; }
        public void setHttpx(String httpx) { this.httpx = httpx; }
        public String getNmap() { return nmap; }
        public void setNmap(String nmap) { this.nmap = nmap; }
        public String getWhatweb() { return whatweb; }
        public void setWhatweb(String whatweb) { this.whatweb = whatweb; }
        public String getNuclei() { return nuclei; }
        public void setNuclei(String nuclei) { this.nuclei = nuclei; }
    }
}
