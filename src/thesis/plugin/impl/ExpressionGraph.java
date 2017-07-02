package thesis.plugin.impl;

import java.util.*;

/**
 * Copyright Maria Igescu
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class ExpressionGraph {
    String expression;
    String rootId;
    Map<String, String> edges = new HashMap<>();
    Map<String, String> methodParametersMap = new HashMap<>();
    // the key should be the uuid and the value the node from the expression
    // we need this since we can have 2-3 nodes of filter sort etc. in just one expression
    // and we don't want to override them
    Map<String, String> aliases = new HashMap<>();


    public ExpressionGraph(String expression) {
        this.expression = expression;
        buildExpressionGraph(expression);
    }


    public boolean containsDirectChain(String node1, String node2) {
        for (Map.Entry<String, String> edge : edges.entrySet()) {
            String key = aliases.get(edge.getKey());
            String value = aliases.get(edge.getValue());
            if (key.equals(node1) && value.equals(node2)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getNodes() {
        return new ArrayList<>(aliases.values());
    }

    public List<String> getParametersFor(String node) {
        if (node == null || node.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> returnList = new ArrayList<>();

        for (Map.Entry<String, String> entry : methodParametersMap.entrySet()) {
            if (node.equals(aliases.get(entry.getKey()))) {
                String exactedValueWithoutBraces = entry.getValue().substring(1, entry.getValue().length() - 1);
                returnList.add(exactedValueWithoutBraces);
            }
        }
        return returnList;
    }

    private void visitRedundantChain(String parentId, String childId, RedundantChainDetails data, String node) {
        if (childId == null || childId.isEmpty()) {
            // not found
            return;
        }
        String nextChildId = edges.get(childId);
        String parentNode = aliases.get(parentId);
        String childNode = aliases.get(childId);
        String nextChildNode = nextChildId != null ? aliases.get(nextChildId) : "";
        String value = methodParametersMap.get(childId);
        String exactValueWithoutBraces = value.substring(1, value.length() - 1);


        if (!parentNode.equals(node) &&
                childNode.equals(node) && nextChildNode.equals(node)) {
            // found the first direct chain - start saving data from here
            data.setParentId(parentId);
            data.setFirstIdInChain(childId);
            data.addArgument(exactValueWithoutBraces);
        } else if (parentNode.equals(node) && childNode.equals(node)) {
            data.addArgument(exactValueWithoutBraces);
            if (!nextChildNode.equals(node)) {
                // at the end of the chain
                data.setLastIdInChain(childId);
                if (!nextChildNode.isEmpty()) {
                    data.setLastChildId(nextChildId);
                }
                return;
            }
        }

        visitRedundantChain(childId, nextChildId, data, node);
    }

    String getFinalExpression() {
        StringBuilder newExpression = new StringBuilder(aliases.get(rootId));
        String currentNode = rootId;

        while (edges.get(currentNode) != null) {
            String valueNodeUUID = edges.get(currentNode);
            String valueNode = aliases.get(valueNodeUUID);
            String nodeAttribute = methodParametersMap.get(valueNodeUUID);
            newExpression.append(".").append(valueNode).append(nodeAttribute);
            currentNode = valueNodeUUID;
        }
        return newExpression.toString();
    }


    private void buildExpressionGraph(String expression) {
        if (expression.isEmpty()) {
            return;
        }
        String[] initialList = expression.split("\\.");

        String parent = initialList[0];
        String parentUUID = UUID.randomUUID().toString() + "_" + parent;
        aliases.put(parentUUID, parent);
        this.rootId = parentUUID;

        String child = "";
        for (String s : initialList) {
            if (s.equals(parent)) {
                continue;
            }
            if (!child.isEmpty()) {
                child += ".";
            }
            child += s;

            long countOpenBrace = child.chars().filter(ch -> ch == '(').count();
            long countClosedBrace = child.chars().filter(ch -> ch == ')').count();


            if (countClosedBrace == countOpenBrace) {
                int indexOfOpenBrace = child.indexOf('(');
                String nodeKey = child;
                String nodeValue = "";
                if (indexOfOpenBrace > -1) {
                    nodeKey = child.substring(0, indexOfOpenBrace);
                    nodeValue = child.substring(indexOfOpenBrace);
                }
                String uuid = UUID.randomUUID().toString() + "_" + nodeKey;
                edges.put(parentUUID, uuid);
                aliases.put(uuid, nodeKey);
                methodParametersMap.put(uuid, nodeValue);
                parent = nodeKey;
                parentUUID = uuid;
                child = "";
            }
        }
    }
}
