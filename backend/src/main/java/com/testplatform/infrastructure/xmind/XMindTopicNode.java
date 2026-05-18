package com.testplatform.infrastructure.xmind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class XMindTopicNode {

    private final String name;
    private final String nodeType;
    private final String description;
    private final List<XMindTopicNode> children;

    public XMindTopicNode(String name, String nodeType, String description, List<XMindTopicNode> children) {
        this.name = name;
        this.nodeType = nodeType;
        this.description = description;
        this.children = children == null ? Collections.emptyList() : children;
    }

    public String getName() {
        return name;
    }

    public String getNodeType() {
        return nodeType;
    }

    public String getDescription() {
        return description;
    }

    public List<XMindTopicNode> getChildren() {
        return children;
    }

    public static XMindTopicNode leaf(String name, String nodeType) {
        return new XMindTopicNode(name, nodeType, null, Collections.emptyList());
    }

    public static XMindTopicNode branch(String name, String nodeType, List<XMindTopicNode> children) {
        return new XMindTopicNode(name, nodeType, null, new ArrayList<XMindTopicNode>(children));
    }
}
