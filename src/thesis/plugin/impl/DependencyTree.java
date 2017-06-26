package thesis.plugin.impl;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Maria on 5/11/2017.
 */
public class DependencyTree {
    List<String> nodes = new ArrayList<>();

    List<String> dot = new ArrayList<>();

    public DependencyTree(Map<String, List<String>> inheritorsMap) {
        nodes = inheritorsMap.keySet().stream().collect(Collectors.toList());

        for (Map.Entry<String, List<String>> entry : inheritorsMap.entrySet()) {
            if (entry.getValue().isEmpty()) {
                dot.add(prettyPrint(entry.getKey()));
            }
            for (String inheritors : entry.getValue()) {
                dot.add(prettyPrint(entry.getKey()) + " -> " + prettyPrint(inheritors));
            }
        }
    }

    private String prettyPrint(String value) {
        return "\"" + value + "\"";
    }


    public String printDOTGraph() {
        String fullString = "digraph G {" + "\n";
        for (int i = 0; i < dot.size() - 1; i++) {
            fullString += dot.get(i) + ",\n";
        }

        fullString += dot.get(dot.size() - 1) + "\n";
        fullString += "}";
        return fullString;
    }
}
