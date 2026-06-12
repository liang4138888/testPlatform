package com.testplatform.infrastructure.xmind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class XMindRoundTripTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final XMindParser parser = new XMindParser(objectMapper);
    private final XMindExporter exporter = new XMindExporter(objectMapper);

    @Test
    void shouldRoundTripParseExportAndParseAgain() throws Exception {
        List<XMindTopicNode> original = parser.parse(sampleArchive());
        byte[] exported = exporter.export("登录用例集", original);

        assertTrue(containsZipEntry(exported, "content.json"));
        assertTrue(containsZipEntry(exported, "metadata.json"));
        assertTrue(containsZipEntry(exported, "manifest.json"));

        List<XMindTopicNode> reparsed = parser.parse(new ByteArrayInputStream(exported));
        assertEquals(flattenNames(original), flattenNames(reparsed));
        assertEquals(flattenTypes(original), flattenTypes(reparsed));
    }

    @Test
    void shouldPreserveNestedStructureAfterExport() throws Exception {
        List<XMindTopicNode> original = parser.parse(sampleArchive());
        List<XMindTopicNode> reparsed = parser.parse(new ByteArrayInputStream(exporter.export("测试", original)));

        assertEquals(1, reparsed.size());
        assertEquals("登录模块", reparsed.get(0).getName());
        assertEquals(1, reparsed.get(0).getChildren().size());
        assertEquals("密码登录成功", reparsed.get(0).getChildren().get(0).getName());
        XMindTopicNode caseNode = reparsed.get(0).getChildren().get(0);
        XMindTopicNode stepNode = caseNode.getChildren().get(0);
        assertEquals("输入账号密码", stepNode.getName());
        assertEquals("case", stepNode.getNodeType());
        assertEquals("进入首页", stepNode.getChildren().get(0).getName());
        assertEquals("case", stepNode.getChildren().get(0).getNodeType());
    }

    private InputStream sampleArchive() throws Exception {
        String contentJson = "[{\"id\":\"sheet\",\"title\":\"画布\",\"rootTopic\":{\"id\":\"root\",\"title\":\"中心主题\","
            + "\"children\":{\"attached\":[{\"id\":\"m1\",\"title\":\"登录模块\",\"children\":{\"attached\":["
            + "{\"id\":\"c1\",\"title\":\"密码登录成功\",\"children\":{\"attached\":["
            + "{\"id\":\"s1\",\"title\":\"输入账号密码\",\"children\":{\"attached\":["
            + "{\"id\":\"e1\",\"title\":\"进入首页\"}"
            + "]}}]}}]}}]}}}]}]";
        return toZipInputStream("content.json", contentJson);
    }

    private String flattenNames(List<XMindTopicNode> nodes) {
        StringBuilder builder = new StringBuilder();
        appendNames(builder, nodes);
        return builder.toString();
    }

    private void appendNames(StringBuilder builder, List<XMindTopicNode> nodes) {
        for (XMindTopicNode node : nodes) {
            builder.append(node.getName()).append('|');
            appendNames(builder, node.getChildren());
        }
    }

    private String flattenTypes(List<XMindTopicNode> nodes) {
        StringBuilder builder = new StringBuilder();
        appendTypes(builder, nodes);
        return builder.toString();
    }

    private void appendTypes(StringBuilder builder, List<XMindTopicNode> nodes) {
        for (XMindTopicNode node : nodes) {
            builder.append(node.getNodeType()).append('|');
            appendTypes(builder, node.getChildren());
        }
    }

    private boolean containsZipEntry(byte[] archiveBytes, String entryName) throws Exception {
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(archiveBytes));
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entryName.equals(entry.getName())) {
                return true;
            }
        }
        return false;
    }

    private InputStream toZipInputStream(String entryName, String content) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        ZipEntry entry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        zipOutputStream.closeEntry();
        zipOutputStream.finish();
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
