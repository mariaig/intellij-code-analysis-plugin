package thesis.plugin.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Maria on 6/22/2017.
 */
public class RedundantChainExpressionGraph extends ExpressionGraph {
    public RedundantChainExpressionGraph(String expression) {
        super(expression);
    }


    public RedundantChainDetails getDetails(String node) {
        if (!containsDirectChain(node, node)) {
            return null;
        }

        RedundantChainDetails data = new RedundantChainDetails();
        visitRedundantChain(rootId, edges.get(rootId), data, node);
        return data;
    }

    public String getFinalExpression(String node, RedundantChainDetails details, String reducedExpression) {
        // need to remove all the nodes that have filter
        List<String> chainIds = new ArrayList<>();
        getIdsFromTheRedundantChain(details.getParentId(), edges.get(details.getParentId()), details.getLastIdInChain(), chainIds);

        for (String id : chainIds) {
            methodParametersMap.remove(id);
            aliases.remove(id);
            edges.remove(id);
        }

        String newNodeId = UUID.randomUUID().toString() + "_" + node;
        aliases.put(newNodeId, node);
        methodParametersMap.put(newNodeId, reducedExpression);
        edges.put(details.getParentId(), newNodeId);
        if (!details.getLastChildId().isEmpty()) {
            edges.put(newNodeId, details.getLastChildId());
        }
        return getFinalExpression();
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
        int firstOpenBraceIndex = value.indexOf("(");
        int lastClosedBraceIndex = value.lastIndexOf(")");
        String exactValueWithoutBraces = value.substring(firstOpenBraceIndex + 1, lastClosedBraceIndex);


        if (!parentNode.equals(node) &&
                childNode.equals(node) && nextChildNode.equals(node)) {
            // found the first direct chain - start saving data from here
            data.setParentId(parentId);
            data.setFirstIdInChain(childId);
            data.addArgument(exactValueWithoutBraces);
        } else if (parentNode.equals(node) && childNode.equals(node)) {
            data.addArgument(exactValueWithoutBraces);
            if(!nextChildNode.equals(node)) {
                // at the end of the chain
                data.setLastIdInChain(childId);
                if(!nextChildNode.isEmpty()) {
                    data.setLastChildId(nextChildId);
                }
                return;
            }
        }

        visitRedundantChain(childId, nextChildId, data, node);
    }



    private void getIdsFromTheRedundantChain(String parentId, String childId, String lastChildId, List<String> chainIds) {
        if (childId.equals(lastChildId)) {
            return;
        }

        chainIds.add(childId);
        getIdsFromTheRedundantChain(childId, edges.get(childId), lastChildId, chainIds);
    }
}
