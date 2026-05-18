package com.testplatform.infrastructure.xmind;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testplatform.common.exception.BusinessException;

@Component
public class XMindExporter {

    private final ObjectMapper objectMapper;

    public XMindExporter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public byte[] export(String sheetTitle, List<XMindTopicNode> nodes) {
        try {
            ObjectNode rootTopic = objectMapper.createObjectNode();
            rootTopic.put("id", randomId());
            rootTopic.put("title", sheetTitle == null || sheetTitle.trim().isEmpty() ? "中心主题" : sheetTitle);
            rootTopic.put("structureClass", "org.xmind.ui.logic.right");

            ArrayNode attached = objectMapper.createArrayNode();
            for (XMindTopicNode node : nodes) {
                attached.add(toTopicJson(node));
            }
            ObjectNode children = objectMapper.createObjectNode();
            children.set("attached", attached);
            rootTopic.set("children", children);

            ObjectNode sheet = objectMapper.createObjectNode();
            sheet.put("id", randomId());
            sheet.put("title", "画布 1");
            sheet.set("rootTopic", rootTopic);

            ArrayNode content = objectMapper.createArrayNode();
            content.add(sheet);

            String metadata = "{\"creator\":{\"name\":\"test-platform\",\"version\":\"0.1.0\"},\"dataStructureVersion\":\"2\"}";
            String manifest = "{\"file-entries\":{\"content.json\":{},\"metadata.json\":{}}}";

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            writeEntry(zipOutputStream, "content.json", objectMapper.writeValueAsBytes(content));
            writeEntry(zipOutputStream, "metadata.json", metadata.getBytes(StandardCharsets.UTF_8));
            writeEntry(zipOutputStream, "manifest.json", manifest.getBytes(StandardCharsets.UTF_8));
            zipOutputStream.finish();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException("XMIND_EXPORT_FAILED", "XMind 导出失败");
        }
    }

    private ObjectNode toTopicJson(XMindTopicNode node) {
        ObjectNode topic = objectMapper.createObjectNode();
        topic.put("id", randomId());
        topic.put("title", node.getName());
        if (node.getDescription() != null && !node.getDescription().trim().isEmpty()) {
            ObjectNode notes = objectMapper.createObjectNode();
            notes.put("plain", node.getDescription());
            topic.set("notes", notes);
        }
        if (!node.getChildren().isEmpty()) {
            ArrayNode attached = objectMapper.createArrayNode();
            for (XMindTopicNode child : node.getChildren()) {
                attached.add(toTopicJson(child));
            }
            ObjectNode children = objectMapper.createObjectNode();
            children.set("attached", attached);
            topic.set("children", children);
        }
        return topic;
    }

    private void writeEntry(ZipOutputStream zipOutputStream, String name, byte[] content) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(content);
        zipOutputStream.closeEntry();
    }

    private String randomId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
