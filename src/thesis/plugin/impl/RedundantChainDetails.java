package thesis.plugin.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Maria on 6/10/2017.
 */
public class RedundantChainDetails {
    private String parentId = "";
    private String firstIdInChain = "";
    private String lastIdInChain = "";
    private String lastChildId = "";
    private List<String> chainArguments = new ArrayList<>();

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getFirstIdInChain() {
        return firstIdInChain;
    }

    public void setFirstIdInChain(String firstIdInChain) {
        this.firstIdInChain = firstIdInChain;
    }

    public String getLastIdInChain() {
        return lastIdInChain;
    }

    public void setLastIdInChain(String lastIdInChain) {
        this.lastIdInChain = lastIdInChain;
    }

    public String getLastChildId() {
        return lastChildId;
    }

    public void setLastChildId(String lastChildId) {
        this.lastChildId = lastChildId;
    }

    public List<String> getChainArguments() {
        return chainArguments;
    }

    public void addArgument(String arg) {
        chainArguments.add(arg);
    }
}
