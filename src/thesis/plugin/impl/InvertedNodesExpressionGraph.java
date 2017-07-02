package thesis.plugin.impl;

import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Maria on 6/22/2017.
 */
public class InvertedNodesExpressionGraph extends ExpressionGraph{

    public InvertedNodesExpressionGraph(String expression) {
        super(expression);
    }

    // since we can have multiple nodes with the same key (ex: filter) in one expression,
    // we really can't be sure which one to invert so we'll cover here just the case when
    // 2 nodes are in a direct chain (after node1 is node2)
    public String getFinalExpression(String node1, String node2) {
        if (!containsDirectChain(node1, node2)) {
            return expression;
        }
        String node1ParentUUID = "";
        String node1UUID = "";
        String node2UUID = "";
        String node2ChildUUID = "";
        // here we need to take the uuid of each node in order to know what exactly we need to invert
        for (Map.Entry<String, String> edge : edges.entrySet()) {
            String key = aliases.get(edge.getKey());
            String value = aliases.get(edge.getValue());
            if (value.equals(node1)) {
                // here I have the parent
                node1ParentUUID = edge.getKey();
            } else if (key.equals(node1) && value.equals(node2)) {
                node1UUID = edge.getKey();
                node2UUID = edge.getValue();
            }

            if (!node1UUID.isEmpty() && !node1ParentUUID.isEmpty() && !node2UUID.isEmpty()) {
                break;
            }
        }

        if(!edges.get(node2UUID).isEmpty()){
            // we can get the child
            node2ChildUUID = edges.get(node2UUID);
        }
        // just repopulate the map with the new order
        edges.put(node1ParentUUID, node2UUID);
        edges.put(node2UUID, node1UUID);
        if (!Strings.isNullOrEmpty(node2ChildUUID)) {
            // ex: ....sorted().filter(..); - node2 = filter (last one) => sorted will be the final node
            edges.put(node1UUID, node2ChildUUID);
        } else {
            // remove the old reference
            edges.remove(node1UUID);
        }


        return getFinalExpression();
    }

}
