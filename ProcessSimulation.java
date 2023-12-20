import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

class Event implements Comparable<Event> {
    enum EventType {
        PROCESS_ARRIVAL, PROCESS_COMPLETION, IO_COMPLETION
    }

    EventType type;
    int time;
    int processNumber;

    public Event(EventType type, int time, int processNumber) {
        this.type = type;
        this.time = time;
        this.processNumber = processNumber;
    }

    public int compareTo(Event other) {
        return Integer.compare(this.time, other.time);
    }
}

class ProcessControlBlock {
    int processNumber;
    int executionTime;
    int remainingBurstTime;

    public ProcessControlBlock(int processNumber, int executionTime) {
        this.processNumber = processNumber;
        this.executionTime = executionTime;
        this.remainingBurstTime = executionTime;
    }
}


public class ProcessSimulation {
    private static List<String> fcfsExperimentResults = new ArrayList<>();
    private static List<String> sjfExperimentResults = new ArrayList<>();
    private static List<String> rrExperimentResults = new ArrayList<>();
    private static int simulationTime = 100; // Set your desired simulation time
    private static int numProcesses = 11; // Number of processes


    // Queues for simulation
    private static Queue<ProcessControlBlock> readyQueue = new LinkedList<>();
    private static Queue<ProcessControlBlock> ioQueue = new LinkedList<>();

    // Event heap for managing events
    private static PriorityQueue<Event> eventHeap = new PriorityQueue<>();

    public static void main(String[] args) {
        int[] executionTimes = generateUniformDistribution(2, 5, numProcesses);
        int[] ioIntervals = generateExponentialDistribution(30, numProcesses);
        int ioOperationTime = 65;

        // Data structures for simulation
        Queue<ProcessControlBlock> readyQueue = new LinkedList<>();
        Queue<ProcessControlBlock> ioQueue = new LinkedList<>();

        // Event heap for managing events
        PriorityQueue<Event> eventHeap = new PriorityQueue<>();

        // Initialize processes
        for (int i = 0; i < numProcesses; i++) {
            readyQueue.add(new ProcessControlBlock(i + 1, executionTimes[i]));
        }

        // Simulation loop
        while (!readyQueue.isEmpty() || !ioQueue.isEmpty() || !eventHeap.isEmpty()) {
            // Handle process arrivals
            if (!readyQueue.isEmpty()) {
                ProcessControlBlock arrivingProcess = readyQueue.poll();
                eventHeap.add(new Event(Event.EventType.PROCESS_ARRIVAL, arrivingProcess.executionTime, arrivingProcess.processNumber));
            }

            // Handle events
            if (!eventHeap.isEmpty()) {
                Event currentEvent = eventHeap.poll();
                switch (currentEvent.type) {
                    case PROCESS_ARRIVAL:
                        handleProcessArrival(currentEvent.processNumber, ioIntervals);
                        break;
                    case PROCESS_COMPLETION:
                        handleProcessCompletion(currentEvent.processNumber, ioQueue);
                        break;
                    case IO_COMPLETION:
                        handleIOCompletion(currentEvent.processNumber, readyQueue);
                        break;
                }
            }
        } // Calculate and print statistics
        double cpuUtilization = calculateCPUUtilization();
        double throughput = calculateThroughput();
        double turnaroundTime = calculateTurnaroundTime();
        double avgWaitingTime = calculateAverageWaitingTime();

        System.out.println("CPU Utilization: " + cpuUtilization);
        System.out.println("Throughput: " + throughput);
        System.out.println("Turnaround Time: " + turnaroundTime);
        System.out.println("Average Waiting Time: " + avgWaitingTime);

        // Run experiments with FCFS, SJF, and Round Robin algorithms
        runExperimentFCFS();
        runExperimentSJF();
        runExperimentRoundRobin();
        // Create log files for experiments
        createLogFile("FCFS_Experiment_Log.txt", fcfsExperimentResults);
        createLogFile("SJF_Experiment_Log.txt", sjfExperimentResults);
        createLogFile("RoundRobin_Experiment_Log.txt", rrExperimentResults);
    }
   



    private static void handleProcessArrival(int processNumber, int[] ioIntervals) {
        int nextIOInterval = ioIntervals[processNumber - 1];
        Event ioEvent = new Event(Event.EventType.IO_COMPLETION, nextIOInterval, processNumber);
        eventHeap.add(ioEvent);
    }

    private static void handleProcessCompletion(int processNumber, Queue<ProcessControlBlock> ioQueue) {
        // Assuming the process completed its execution, add it to the IO queue
        ProcessControlBlock completedProcess = new ProcessControlBlock(processNumber, 0);
        ioQueue.add(completedProcess);
    }

    private static void handleIOCompletion(int processNumber, Queue<ProcessControlBlock> readyQueue) {
        ProcessControlBlock completedProcess = new ProcessControlBlock(processNumber, 0);
        readyQueue.add(completedProcess);
    }
    private static double calculateCPUUtilization() {
        // Assuming total time is the simulation time and processes have completed
        int totalTime = simulationTime;
        int totalExecutionTime = 0;

        for (ProcessControlBlock process : readyQueue) {
            totalExecutionTime += process.executionTime;
        }

        for (ProcessControlBlock process : ioQueue) {
            totalExecutionTime += process.executionTime;
        }

        return (double) totalExecutionTime / totalTime;
    }

    private static double calculateThroughput() {
        return (double) numProcesses / simulationTime;
    }

    private static double calculateTurnaroundTime() {
        int totalTurnaroundTime = 0;

        for (ProcessControlBlock process : readyQueue) {
            totalTurnaroundTime += process.executionTime;
        }

        for (ProcessControlBlock process : ioQueue) {
            totalTurnaroundTime += process.executionTime;
        }

        return (double) totalTurnaroundTime / numProcesses;
    }

    private static double calculateAverageWaitingTime() {
        int totalWaitingTime = 0;

        // Calculate waiting time for processes in the ready queue
        for (ProcessControlBlock process : readyQueue) {
            totalWaitingTime += process.executionTime;
        }

        // Calculate waiting time for processes in the IO queue
        for (ProcessControlBlock process : ioQueue) {
            totalWaitingTime += process.executionTime;
        }

        return (double) totalWaitingTime / numProcesses;
    }  
    private static void runExperimentFCFS() {
        // FCFS (First-Come-First-Served) scheduling algorithm

        Queue<ProcessControlBlock> fcfsReadyQueue = new LinkedList<>(readyQueue);
        Queue<ProcessControlBlock> fcfsIOQueue = new LinkedList<>(ioQueue);
        int currentTime = 0;

        while (!fcfsReadyQueue.isEmpty() || !fcfsIOQueue.isEmpty()) {
            // Simulate CPU execution
            if (!fcfsReadyQueue.isEmpty()) {
                ProcessControlBlock currentProcess = fcfsReadyQueue.poll();
                currentTime += currentProcess.executionTime;
            }

            // Simulate I/O completion
            if (!fcfsIOQueue.isEmpty()) {
                ProcessControlBlock ioProcess = fcfsIOQueue.poll();
                currentTime += ioProcess.executionTime;
            }
        }

        double fcfsThroughput = calculateThroughput();
        double fcfsTurnaroundTime = calculateTurnaroundTime();
        double fcfsAvgWaitingTime = calculateAverageWaitingTime();

        fcfsExperimentResults.add("FCFS Throughput: " + fcfsThroughput);
        fcfsExperimentResults.add("FCFS Turnaround Time: " + fcfsTurnaroundTime);
        fcfsExperimentResults.add("FCFS Average Waiting Time: " + fcfsAvgWaitingTime);
    }

    private static void runExperimentSJF() {
        // SJF (Shortest-Job-First) scheduling algorithm

        // Sort the readyQueue based on execution time (shortest job first)
        List<ProcessControlBlock> sjfReadyList = new ArrayList<>(readyQueue);
        sjfReadyList.sort(Comparator.comparingInt(process -> process.executionTime));

        Queue<ProcessControlBlock> sjfReadyQueue = new LinkedList<>(sjfReadyList);
        Queue<ProcessControlBlock> sjfIOQueue = new LinkedList<>(ioQueue);
        int currentTime = 0;

        while (!sjfReadyQueue.isEmpty() || !sjfIOQueue.isEmpty()) {
            // Simulate CPU execution
            if (!sjfReadyQueue.isEmpty()) {
                ProcessControlBlock currentProcess = sjfReadyQueue.poll();
                currentTime += currentProcess.executionTime;
            }

            // Simulate I/O completion
            if (!sjfIOQueue.isEmpty()) {
                ProcessControlBlock ioProcess = sjfIOQueue.poll();
                currentTime += ioProcess.executionTime;
            }
        }

        double sjfThroughput = calculateThroughput();
        double sjfTurnaroundTime = calculateTurnaroundTime();
        double sjfAvgWaitingTime = calculateAverageWaitingTime();

        sjfExperimentResults.add("SJF Throughput: " + sjfThroughput);
        sjfExperimentResults.add("SJF Turnaround Time: " + sjfTurnaroundTime);
        sjfExperimentResults.add("SJF Average Waiting Time: " + sjfAvgWaitingTime);
    }

    private static void createLogFile(String fileName, List<String> experimentResults) {
        // Create a log file and write experiment results
        // This is a basic implementation, you might want to format the results more appropriately
        try {
            java.nio.file.Path filePath = java.nio.file.Paths.get(fileName);
            java.nio.file.Files.write(filePath, experimentResults);
            System.out.println("Log file created: " + filePath.toAbsolutePath());
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static void runExperimentRoundRobin() {
        // Round Robin scheduling algorithm

        // Vary quantum values for experiments
        int[] quantumValues = {10, 20, 30, 40, 50};

        for (int quantum : quantumValues) {
            Queue<ProcessControlBlock> rrReadyQueue = new LinkedList<>(readyQueue);
            Queue<ProcessControlBlock> rrIOQueue = new LinkedList<>(ioQueue);
            int currentTime = 0;

            while (!rrReadyQueue.isEmpty() || !rrIOQueue.isEmpty()) {
                // Simulate CPU execution with Round Robin
                if (!rrReadyQueue.isEmpty()) {
                    ProcessControlBlock currentProcess = rrReadyQueue.poll();
                    int executionTime = Math.min(quantum, currentProcess.remainingBurstTime);
                    currentProcess.remainingBurstTime -= executionTime;
                    currentTime += executionTime;

                    // Check if the process needs further execution or goes to I/O
                    if (currentProcess.remainingBurstTime > 0) {
                        rrReadyQueue.add(currentProcess);
                    } else {
                        // Process completed, simulate I/O completion
                        Event ioEvent = new Event(Event.EventType.IO_COMPLETION, currentTime + 65, currentProcess.processNumber);
                        eventHeap.add(ioEvent);
                    }
                }

                // Simulate I/O completion
                if (!rrIOQueue.isEmpty()) {
                    ProcessControlBlock ioProcess = rrIOQueue.poll();
                    currentTime += ioProcess.executionTime;
                }
            }

            // Calculate and log Round Robin experiment results
            double rrThroughput = calculateThroughput();
            double rrTurnaroundTime = calculateTurnaroundTime();
            double rrAvgWaitingTime = calculateAverageWaitingTime();

            System.out.println("Round Robin Experiment with Quantum " + quantum);
            System.out.println("Round Robin Throughput: " + rrThroughput);
            System.out.println("Round Robin Turnaround Time: " + rrTurnaroundTime);
            System.out.println("Round Robin Average Waiting Time: " + rrAvgWaitingTime);
        }
    }

    private static int[] generateUniformDistribution(int min, int max, int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = ThreadLocalRandom.current().nextInt(min, max + 1);
        }
        return array;
    }

    private static int[] generateExponentialDistribution(int lambda, int size) {
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = (int) Math.ceil(-Math.log(1 - Math.random()) / lambda);
        }
        return array;
    }

}