package thesis.plugin.impl;

import java.util.ArrayList;
import java.util.List;

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
