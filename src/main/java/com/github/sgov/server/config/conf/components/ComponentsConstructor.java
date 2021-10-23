package com.github.sgov.server.config.conf.components;

import java.util.stream.Collectors;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

public class ComponentsConstructor extends Constructor {
    private final TypeDescription itemType = new TypeDescription(ComponentConf.class);

    public ComponentsConstructor() {
        this.rootTag = new Tag("myRoot");
        this.addTypeDescription(itemType);
    }

    @Override
    protected Object constructObject(Node node) {
        if ("myRoot".equals(node.getTag().getValue()) && node instanceof MappingNode) {
            MappingNode mappingNode = (MappingNode) node;
            return mappingNode.getValue().stream().collect(
                Collectors.toMap(
                    t -> super.constructObject(t.getKeyNode()),
                    t -> {
                        Node child = t.getValueNode();
                        child.setType(itemType.getType());
                        return super.constructObject(child);
                    }
                )
            );

        } else {
            return super.constructObject(node);
        }
    }
}
