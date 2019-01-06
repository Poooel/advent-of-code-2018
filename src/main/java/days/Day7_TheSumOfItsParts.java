package days;

import launcher.ChallengeHelper;
import launcher.Executable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

public class Day7_TheSumOfItsParts implements Executable {
    @Override
    public String executePartOne() {
        List<String> inputs = ChallengeHelper.readInputData(7);

        // Compute the directed graph from the input
        List<Node> graph = computeDirectedGraph(inputs);
        // Sort the node using a topological sort (should be)
        // https://en.wikipedia.org/wiki/Topological_sorting
        List<Node> topologicallySorted = topologicalSort(graph);

        return printResult(topologicallySorted);
    }

    @Override
    public String executePartTwo() {
        List<String> inputs = ChallengeHelper.readInputData(7);

        // Compute the directed graph from the input
        List<Node> graph = computeDirectedGraph(inputs);
        // Compute the time it took to complete all of the jobs
        return String.valueOf(computeTimeOfWork(graph));
    }

    private List<Node> computeDirectedGraph(List<String> inputs) {
        // We use a map because it is easier to create the graph with
        Map<String, Node> graph = new HashMap<>();

        for (String input : inputs) {
            // Split the input line on the spaces
            String[] splitInput = input.split(" ");
            String source = splitInput[1];
            String target = splitInput[7];
            // "Step C must be finished before step A can begin."
            // ["Step", "C", "must", "be", "finished", "before", "step", "A", "can", "begin."]
            //           ↥                                                ↥
            //         Source                                           Target

            // If the key is absent put the value, otherwise return the value
            graph.putIfAbsent(source, new Node(source));
            graph.putIfAbsent(target, new Node(target));

            // Then link the source node with the target node
            graph.get(source).link(graph.get(target));
        }

        return new ArrayList<>(graph.values());
    }

    private void sortQueue(List<Node> queue) {
        // Sort the queue by the name of the node in alphabetical order
        queue.sort(Comparator.comparing(item -> item.name));
    }

    private boolean allHaveBeenVisited(List<Node> nodes) {
        for (Node node : nodes) {
            if (!node.isHasBeenVisited()) {
                // If one of the nodes has not been visited then return false immediately
                return false;
            }
        }

        // If all the nodes have been visited then return true
        return true;
    }

    private List<Node> findRootNodes(List<Node> graph) {
        return graph
            // Stream them
            .stream()
            // To find the root nodes we need the ones with no parents
            .filter(node -> node.getParents().size() == 0)
            // Collect them into a list
            .collect(Collectors.toList());
    }

    private String printResult(List<Node> nodes) {
        StringBuilder stringBuilder = new StringBuilder();

        for (Node node : nodes) {
            // Append the name of all the nodes to a string
            stringBuilder.append(node.getName());
        }

        return stringBuilder.toString();
    }

    private List<Node> topologicalSort(List<Node> directedGraph) {
        // Find the root nodes to begin the sorting
        List<Node> rootNodes = findRootNodes(directedGraph);

        // Create a queue of nodes to visit
        LinkedList<Node> queue = new LinkedList<>();
        // Add the root nodes to the queue as this is with them that we start
        rootNodes.forEach(queue::offer);
        // Sort the queue because the order should be alphabetical if the queue contains
        // more than one item
        sortQueue(queue);

        // Create the list that will hold the sorted nodes
        List<Node> sortedNodes = new ArrayList<>();

        // While the queue has items continue to process
        // when the queue will be empty, it means we reached the end of the graph
        while (queue.size() > 0) {
            // Poll the queue to get the head
            Node currentNode = queue.poll();

            // Add the node to the sorted list
            sortedNodes.add(currentNode);

            // Set the node to visited
            currentNode.setHasBeenVisited(true);

            // For each child of the current node
            for (Node child : currentNode.getChilds()) {
                // If all the parents of the node have been visited
                if (allHaveBeenVisited(child.getParents())) {
                    // Then add it to the queue
                    queue.offer(child);
                }
            }

            // Sort the queue again for the same reason as before
            sortQueue(queue);
        }

        return sortedNodes;
    }

    private int computeTimeOfWork(List<Node> graph) {
        // The concurrent workers constant
        int concurrentWorkers = 5;
        // The work queue to hold the jobs
        ArrayBlockingQueue<Job> workQueue = new ArrayBlockingQueue<>(concurrentWorkers);
        // The queue of work
        LinkedList<Node> queue = new LinkedList<>();
        // The time total of the work
        int totalTimeTook = 0;

        List<Node> rootNodes = findRootNodes(graph);
        // Add the root nodes to the queue as this is with them that we start
        rootNodes.forEach(queue::offer);
        // Sort the queue because the order should be alphabetical if the queue contains
        // more than one item
        sortQueue(queue);

        do {
            // While the workQueue has space try to fill it
            while (workQueue.size() < 5) {
                // While the queue has item try to get them
                if (queue.size() > 0) {
                    // Take the head of the queue
                    Node preliminaryJob = queue.poll();
                    // Create a job from it
                    Job job = new Job(preliminaryJob, getTimeOfWork(preliminaryJob.getName()));
                    // Add the job to the workQueue
                    workQueue.offer(job);
                } else {
                    // Break when the queue is empty
                    break;
                }
            }

            WorkContext workContext = doWork(workQueue);
            // Get the job that have finished
            List<Node> finishedJobs = workContext.getFinishedJobs();
            // Add the time it took to the total counter
            totalTimeTook += workContext.timeTook;

            // Sort the queue of finished job if there is several ones
            sortQueue(finishedJobs);

            // For each finished job mark the underlying node visited (this is the topological
            // sort happening)
            finishedJobs.forEach(finishedJob -> finishedJob.setHasBeenVisited(true));

            // Same bit of code as the topological sort
            for (Node finishedJob : finishedJobs) {
                for (Node child : finishedJob.getChilds()) {
                    if (allHaveBeenVisited(child.getParents())) {
                        queue.offer(child);
                    }
                }
            }

            sortQueue(queue);
        // Do this while either queue have items in them
        } while (workQueue.size() > 0 || queue.size() > 0);

        // Return the total time it took
        return totalTimeTook;
    }

    private WorkContext doWork(Queue<Job> workQueue) {
        // The list of job finished, one or more may have finished at the same time
        // so we store them into a list
        List<Node> finishedJob = new ArrayList<>();
        // The time it took to finish the jobs
        int timeTook = 0;

        do {
            // Increment the time it took
            timeTook++;
            // For each job
            for (Job job : workQueue) {
                // Decrement the time they have left
                job.decrementWorkTime();

                // If the job finished its time
                if (job.getTime() == 0) {
                    // Add it to the list of finished jobs
                    finishedJob.add(job.node);
                    // And remove it from the workQueue
                    workQueue.remove(job);
                }
            }
        // Do this till, at least, one job finished
        } while (finishedJob.size() == 0);

        // Return the result as a context because we need the time it took and the jobs finished
        return new WorkContext(finishedJob, timeTook);
    }

    private int getTimeOfWork(String name) {
        // Add 60 (for every job) and add the time of the node's name
        // get the ascii code for the name (it is only one letter) and substract 64
        // ascii(A) -> 65; ascii(Z) -> 90 so by removing 64 we get
        // ascii(A) - 64 -> 1; ascii(Z) - 64 -> 26
        return 60 + (((int) name.charAt(0)) - 64);
    }

    @Data
    private class Node {
        private String name;
        private List<Node> parents;
        private List<Node> childs;
        private boolean hasBeenVisited;

        Node(String name) {
            this.name = name;
            this.parents = new ArrayList<>();
            this.childs = new ArrayList<>();
            this.hasBeenVisited = false;
        }

        void link(Node node) {
            this.childs.add(node);
            node.parents.add(this);
        }
    }

    @Data
    @AllArgsConstructor
    private class Job {
        private Node node;
        private int time;

        void decrementWorkTime() {
            time--;
        }
    }

    @Value
    private class WorkContext {
        private List<Node> finishedJobs;
        private int timeTook;
    }
}
