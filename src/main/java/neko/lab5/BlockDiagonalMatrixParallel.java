package neko.lab5;

import mpi.MPI;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BlockDiagonalMatrixParallel {

    // Method to read a matrix from a JSON file
    static int[][] readMatrixFromJson(String filePath, String matrixKey) throws IOException, ParseException {
        return BlockDiagonalMatrixBase.readMatrixFromJson(filePath, matrixKey);
    }

    // Method to print the matrix
    static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.printf("%3d ", val); // Print each value with formatting
            }
            System.out.println();
        }
    }

    // Parallel algorithm to transform the matrix into block diagonal form
    static void algorithm(int[][] matrix, boolean allLogs, int rank, int size) throws Exception {
        int N = matrix.length;

        // Broadcast the matrix data to all processes
        if (rank != 0) {
            matrix = new int[N][N]; // Initialize matrix for non-root processes
        }

        for (int i = 0; i < N; i++) {
            MPI.COMM_WORLD.Bcast(matrix[i], 0, N, MPI.INT, 0); // Broadcast rows of the matrix
        }

        // Calculate the chunk size for each process
        int chunkSize = N / size;
        int remainder = N % size;
        int start = rank * chunkSize + Math.min(rank, remainder); // Start index for this process
        int end = start + chunkSize + (rank < remainder ? 1 : 0) - 1; // End index for this process
        int localNum = end - start + 1; // Number of rows assigned to this process

        // Build the adjacency list for the graph
        List<List<Integer>> adjacencyList = new ArrayList<>(N);
        for (int i = 0; i < N; i++) {
            adjacencyList.add(new ArrayList<>());
        }

        // Populate the adjacency list based on non-zero entries in the matrix
        for (int i = start; i <= end; i++) {
            for (int j = 0; j < N; j++) {
                if (matrix[i][j] != 0 || matrix[j][i] != 0) {
                    adjacencyList.get(i).add(j); // Add edges to the adjacency list
                }
            }
        }

        // Initialize the global component array
        int[] componentGlobal = new int[N];
        for (int i = 0; i < N; i++) {
            componentGlobal[i] = i; // Each vertex initially belongs to its own component
        }

        // Prepare arrays for Allgatherv operation
        int[] recvcounts = new int[size];
        int[] displs = new int[size];
        for (int r = 0; r < size; r++) {
            int rChunk = N / size;
            int rRemainder = N % size;
            displs[r] = r * rChunk + Math.min(r, rRemainder); // Displacement for process r
            recvcounts[r] = rChunk + (r < rRemainder ? 1 : 0); // Number of elements for process r
        }

        boolean changed;
        do {
            changed = false;
            int[] componentNew = Arrays.copyOf(componentGlobal, N); // Copy the current component array

            // Update components for vertices assigned to this process
            for (int i = start; i <= end; i++) {
                int minComp = componentGlobal[i];
                for (int neighbor : adjacencyList.get(i)) {
                    if (componentGlobal[neighbor] < minComp) {
                        minComp = componentGlobal[neighbor]; // Find the minimum component ID
                    }
                }
                if (minComp < componentNew[i]) {
                    componentNew[i] = minComp; // Update the component ID
                    changed = true; // Mark that a change occurred
                }
            }

            // Gather updated components from all processes
            int[] buffer = new int[N];
            MPI.COMM_WORLD.Allgatherv(componentNew, start, localNum, MPI.INT, buffer, 0, recvcounts, displs, MPI.INT);

            boolean globalChanged = false;
            for (int i = 0; i < N; i++) {
                if (buffer[i] < componentGlobal[i]) {
                    componentGlobal[i] = buffer[i]; // Update the global component array
                    globalChanged = true; // Mark that a global change occurred
                }
            }

            // Check if any process detected a change
            int[] changedArray = {globalChanged ? 1 : 0};
            int[] result = new int[1];
            MPI.COMM_WORLD.Allreduce(changedArray, 0, result, 0, 1, MPI.INT, MPI.BOR);
            changed = changedArray[0] == 1;

        } while (changed); // Repeat until no changes occur

        // Root process sorts vertices by their component IDs
        int[] permutation = new int[N];
        if (rank == 0) {
            for (int i = 0; i < N; i++) {
                permutation[i] = i;
            }
            permutation = Arrays.stream(permutation)
                    .boxed()
                    .sorted(Comparator.comparingInt(a -> componentGlobal[a])) // Sort by component ID
                    .mapToInt(i -> i)
                    .toArray();
        }

        // Broadcast the permutation array to all processes
        MPI.COMM_WORLD.Bcast(permutation, 0, N, MPI.INT, 0);

        // Form the transformed matrix based on the sorted order of vertices
        int[][] sortedMatrix = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sortedMatrix[i][j] = matrix[permutation[i]][permutation[j]];
            }
        }

        // Output results from the root process
        if (rank == 0) {
            System.out.print("\nVertex order for block diagonal form: ");
            for (int idx : permutation) {
                System.out.print(idx + " "); // Print the order of vertices
            }
            System.out.println();

            if (allLogs) {
                System.out.println("\nTransformed Matrix (Block Diagonal Form):");
                printMatrix(sortedMatrix); // Print the transformed matrix
            }
        }
    }

    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank(); // Get the rank of the current process
        int size = MPI.COMM_WORLD.Size(); // Get the total number of processes

        boolean logs = false; // Set to true to enable detailed logs

        // List of matrices to process
        ArrayList<String> matrixList = new ArrayList<>();
        matrixList.add("init_matrix");
        matrixList.add("matrix6");
        matrixList.add("matrix10");
        matrixList.add("matrix25");
        matrixList.add("matrix50");
        matrixList.add("matrix100");
        matrixList.add("matrix200");
        matrixList.add("matrix300");
        matrixList.add("matrix400");

        // Sizes of the matrices
        ArrayList<Integer> matrixSizes = new ArrayList<>();
        matrixSizes.add(6);
        matrixSizes.add(6);
        matrixSizes.add(10);
        matrixSizes.add(25);
        matrixSizes.add(50);
        matrixSizes.add(100);
        matrixSizes.add(200);
        matrixSizes.add(300);
        matrixSizes.add(400);

        int[][] matrix;
        String matrixName;
        int matrixSize;

        long startTime;
        long endTime;
        long duration;

        // Process each matrix in the list
        for (int i = 0; i < matrixList.size(); i++) {
            matrixName = matrixList.get(i);
            matrixSize = matrixSizes.get(i);

            if (rank == 0) {
                matrix = readMatrixFromJson("src/main/resources/test_matrix.json", matrixName);

                if (logs) {
                    System.out.println("[" + matrixName + "] Original Matrix:");
                    printMatrix(matrix); // Print the original matrix
                } else {
                    System.out.print("[" + matrixName + "]");
                }
            } else {
                matrix = new int[matrixSize][matrixSize]; // Initialize matrix for non-root processes
            }

            startTime = System.nanoTime(); // Start timing the algorithm

            algorithm(matrix, logs, rank, size); // Run the parallel algorithm

            endTime = System.nanoTime(); // End timing the algorithm
            duration = (endTime - startTime) / 1000; // Calculate duration in microseconds

            if (rank == 0) {
                System.out.println("Execution Time: " + duration + " μs"); // Print the execution time
            }
        }
        MPI.Finalize(); // Finalize MPI
    }
}