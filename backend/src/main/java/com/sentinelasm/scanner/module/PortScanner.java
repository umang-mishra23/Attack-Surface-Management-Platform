package com.sentinelasm.scanner.module;

import com.sentinelasm.scanner.ScanResult;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(20)
public class PortScanner implements ScannerModule {
    @Override
    public String name() {
        return "Running Port Scanner";
    }

    @Override
    public void execute(ScannerContext context) throws Exception {
        Path workspace = Files.createDirectories(Path.of(context.properties().getWorkspace(), "ports-" + System.currentTimeMillis()));
        for (ScannerContext.AssetBuilder asset : context.assets()) {
            context.log("Running Nmap for " + asset.hostname());
            Path xml = workspace.resolve(asset.hostname().replaceAll("[^a-zA-Z0-9.-]", "_") + ".xml");
            context.executor().run(
                    List.of(context.properties().getTools().getNmap(), "-sV", "--open", "-oX", xml.toString(), asset.hostname()),
                    workspace,
                    context.timeout(),
                    line -> context.log("[Nmap] " + line)
            );
            parse(xml).forEach(asset::addPort);
        }
    }

    private List<ScanResult.PortFinding> parse(Path xml) throws Exception {
        List<ScanResult.PortFinding> ports = new ArrayList<>();
        if (!Files.exists(xml)) return ports;
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xml.toFile());
        NodeList nodes = document.getElementsByTagName("port");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element port = (Element) nodes.item(i);
            Element state = (Element) port.getElementsByTagName("state").item(0);
            Element service = (Element) port.getElementsByTagName("service").item(0);
            ports.add(new ScanResult.PortFinding(
                    Integer.parseInt(port.getAttribute("portid")),
                    port.getAttribute("protocol"),
                    service == null ? "" : service.getAttribute("name"),
                    service == null ? "" : nonBlank(service.getAttribute("product"), service.getAttribute("version")),
                    state == null ? "unknown" : state.getAttribute("state")
            ));
        }
        return ports;
    }

    private String nonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}
