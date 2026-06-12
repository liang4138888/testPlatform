package com.testplatform.infrastructure.xmind;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.common.exception.BusinessException;

class XMindParserTest {

    private final XMindParser parser = new XMindParser(new ObjectMapper());

    @Test
    void shouldParseModernXmindContentJson() throws Exception {
        String contentJson = "[{\"id\":\"sheet\",\"title\":\"画布\",\"rootTopic\":{\"id\":\"root\",\"title\":\"中心主题\","
            + "\"children\":{\"attached\":[{\"id\":\"m1\",\"title\":\"登录模块\",\"children\":{\"attached\":["
            + "{\"id\":\"c1\",\"title\":\"密码登录成功\",\"children\":{\"attached\":["
            + "{\"id\":\"s1\",\"title\":\"输入账号密码\",\"children\":{\"attached\":["
            + "{\"id\":\"e1\",\"title\":\"进入首页\"}"
            + "]}}]}}]}}]}}}]}]";

        InputStream inputStream = toZipInputStream("content.json", contentJson);
        List<XMindTopicNode> nodes = parser.parse(inputStream);

        assertEquals(1, nodes.size());
        assertEquals("case", nodes.get(0).getNodeType());
        assertEquals("登录模块", nodes.get(0).getName());
        assertEquals("case", nodes.get(0).getChildren().get(0).getNodeType());
        assertEquals("case", nodes.get(0).getChildren().get(0).getChildren().get(0).getNodeType());
        assertEquals(
            "case",
            nodes.get(0).getChildren().get(0).getChildren().get(0).getChildren().get(0).getNodeType()
        );
    }

    @Test
    void shouldRejectLegacyContentXmlOnlyArchive() throws Exception {
        InputStream inputStream = toZipInputStream("content.xml", "<xmap-content></xmap-content>");
        BusinessException exception = assertThrows(BusinessException.class, () -> parser.parse(inputStream));
        assertEquals("UNSUPPORTED_XMIND_VERSION", exception.getCode());
    }

    @Test
    void shouldDefaultImportedNodeTypeToCase() {
        assertEquals("case", XMindParser.nodeTypeForDepth(1));
        assertEquals("case", XMindParser.nodeTypeForDepth(2));
        assertEquals("case", XMindParser.nodeTypeForDepth(3));
        assertEquals("case", XMindParser.nodeTypeForDepth(4));
        assertEquals("case", XMindParser.nodeTypeForDepth(5));
    }

    private InputStream toZipInputStream(String entryName, String content) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        ZipEntry entry = new ZipEntry(entryName);
        zipOutputStream.putNextEntry(entry);
        zipOutputStream.write(content.getBytes(StandardCharsets.UTF_8));
        zipOutputStream.closeEntry();
        zipOutputStream.finish();
        return new java.io.ByteArrayInputStream(outputStream.toByteArray());
    }
}
