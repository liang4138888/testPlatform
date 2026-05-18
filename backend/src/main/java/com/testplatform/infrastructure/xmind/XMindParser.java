package com.testplatform.infrastructure.xmind;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.common.exception.BusinessException;

@Component
public class XMindParser {

    private static final String CONTENT_JSON = "content.json";
    private static final String CONTENT_XML = "content.xml";

    private final ObjectMapper objectMapper;

    public XMindParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<XMindTopicNode> parse(InputStream inputStream) {
        byte[] archiveBytes = readAll(inputStream);
        if (containsEntry(archiveBytes, CONTENT_XML) && !containsEntry(archiveBytes, CONTENT_JSON)) {
            throw new BusinessException("UNSUPPORTED_XMIND_VERSION", "不支持旧版 XMind（content.xml），请使用包含 content.json 的新版文件");
        }

        String contentJson = readEntry(archiveBytes, CONTENT_JSON);
        if (contentJson == null || contentJson.trim().isEmpty()) {
            throw new BusinessException("INVALID_XMIND_FILE", "无效的 XMind 文件：缺少 content.json");
        }

        try {
            JsonNode sheets = objectMapper.readTree(contentJson);
            if (!sheets.isArray() || sheets.size() == 0) {
                throw new BusinessException("INVALID_XMIND_CONTENT", "content.json 格式无效");
            }
            JsonNode rootTopic = sheets.get(0).path("rootTopic");
            if (rootTopic.isMissingNode()) {
                throw new BusinessException("INVALID_XMIND_CONTENT", "content.json 缺少 rootTopic");
            }
            JsonNode attached = rootTopic.path("children").path("attached");
            if (!attached.isArray() || attached.size() == 0) {
                throw new BusinessException("EMPTY_XMIND_TEMPLATE", "XMind 模板为空，请至少包含一级主题");
            }

            List<XMindTopicNode> nodes = new ArrayList<XMindTopicNode>();
            for (int index = 0; index < attached.size(); index++) {
                nodes.add(parseTopic(attached.get(index), 1));
            }
            return nodes;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("XMIND_PARSE_FAILED", "XMind 解析失败");
        }
    }

    private XMindTopicNode parseTopic(JsonNode topic, int depth) {
        String title = textValue(topic, "title");
        if (title.isEmpty()) {
            throw new BusinessException("INVALID_XMIND_TOPIC", "存在无标题主题节点");
        }

        List<XMindTopicNode> children = new ArrayList<XMindTopicNode>();
        JsonNode attached = topic.path("children").path("attached");
        if (attached.isArray()) {
            for (int index = 0; index < attached.size(); index++) {
                children.add(parseTopic(attached.get(index), depth + 1));
            }
        }

        return new XMindTopicNode(title, nodeTypeForDepth(depth), extractNotes(topic), children);
    }

    static String nodeTypeForDepth(int depth) {
        switch (depth) {
            case 1:
                return "module";
            case 2:
                return "case";
            case 3:
                return "step";
            default:
                return "expected";
        }
    }

    private String extractNotes(JsonNode topic) {
        JsonNode notes = topic.path("notes");
        if (notes.isMissingNode()) {
            return null;
        }
        JsonNode plain = notes.path("plain");
        if (!plain.isMissingNode() && !plain.asText().trim().isEmpty()) {
            return plain.asText().trim();
        }
        JsonNode realHtml = notes.path("realHTML");
        if (!realHtml.isMissingNode() && !realHtml.asText().trim().isEmpty()) {
            return realHtml.asText().trim();
        }
        return null;
    }

    private String textValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText().trim();
    }

    private byte[] readAll(InputStream inputStream) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new BusinessException("XMIND_READ_FAILED", "读取 XMind 文件失败");
        }
    }

    private boolean containsEntry(byte[] archiveBytes, String entryName) {
        return readEntry(archiveBytes, entryName) != null;
    }

    private String readEntry(byte[] archiveBytes, String entryName) {
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new java.io.ByteArrayInputStream(archiveBytes));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entryName.equalsIgnoreCase(entry.getName()) || entry.getName().endsWith("/" + entryName)) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = zipInputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    return new String(outputStream.toByteArray(), StandardCharsets.UTF_8);
                }
            }
            return null;
        } catch (IOException exception) {
            throw new BusinessException("XMIND_READ_FAILED", "读取 XMind 压缩包失败");
        }
    }
}
