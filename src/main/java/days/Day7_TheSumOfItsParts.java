package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.Value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Day7_TheSumOfItsParts implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(7);

        HashMap<String, Node> nodes = new HashMap<>();

        for (String input : inputs) {
            String[] splitInput = input.split(" ");
            String source = splitInput[1];
            String target = splitInput[7];

            if (!nodes.containsKey(source)) {
                nodes.put(source, new Node(source));
            }

            if (!nodes.containsKey(target)) {
                nodes.put(target, new Node(target));
            }

            nodes.get(source).link(nodes.get(target));
        }

        List<Node> rootNodes = nodes.values().stream().filter(node -> node.getParents().size() == 0)
            .collect(Collectors.toList());
        List<Node> endNodes = nodes.values().stream().filter(node -> node.getChilds().size() == 0)
            .collect(Collectors.toList());

        return "notYetFound";
    }

    @Override
    public String executePartTwo() {
        return null;
    }

    @Value
    private class Node {
        private String name;
        private List<Node> parents;
        private List<Node> childs;

        Node(String name) {
            this.name = name;
            this.parents = new ArrayList<>();
            this.childs = new ArrayList<>();
        }

        void link(Node node) {
            this.childs.add(node);
            node.parents.add(this);
        }
    }
}
