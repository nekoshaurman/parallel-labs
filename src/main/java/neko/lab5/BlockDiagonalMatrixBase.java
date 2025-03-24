package neko.lab5;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class BlockDiagonalMatrixBase {

    // Method to read a matrix from a JSON file
    static int[][] readMatrixFromJson(String filePath, String matrixKey) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
        JSONArray matrixArray = (JSONArray) jsonObject.get(matrixKey);

        // Initialize the matrix based on the size of the JSON array
        int[][] matrix = new int[matrixArray.size()][((JSONArray) matrixArray.get(0)).size()];
        for (int i = 0; i < matrixArray.size(); i++) {
            JSONArray row = (JSONArray) matrixArray.get(i);
            for (int j = 0; j < row.size(); j++) {
                matrix[i][j] = ((Long) row.get(j)).intValue(); // Convert JSON Long to int
            }
        }
        return matrix;
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

    // Recursive Depth-First Search (DFS) to traverse the graph
    static void dfs(int v, int compId, List<List<Integer>> adjacencyList, boolean[] visited, int[] component) {
        visited[v] = true; // Mark the current vertex as visited
        component[v] = compId; // Assign the component ID to the vertex
        for (int w : adjacencyList.get(v)) {
            if (!visited[w]) { // If the adjacent vertex is not visited, recurse
                dfs(w, compId, adjacencyList, visited, component);
            }
        }
    }

    // Main algorithm to transform the matrix into block diagonal form
    static void algorithm(int[][] matrix, boolean allLogs) throws Exception {
        int N = matrix.length;

        // Build the adjacency list for the graph
        List<List<Integer>> adjacencyList = new ArrayList<>();
        for (int i = 0; i < N; i++) {
            adjacencyList.add(new ArrayList<>());
        }

        // Populate the adjacency list based on non-zero entries in the matrix
        for (int i = 0; i < N; i++) {
            for (int j = i + 1; j < N; j++) {
                if (matrix[i][j] != 0 || matrix[j][i] != 0) {
                    adjacencyList.get(i).add(j);
                    adjacencyList.get(j).add(i);
                }
            }
        }

        // Find connected components using DFS
        boolean[] visited = new boolean[N];
        int[] component = new int[N];
        Arrays.fill(component, -1); // Initialize all components to -1
        int compId = 0;
        for (int i = 0; i < N; i++) {
            if (!visited[i]) { // If the vertex is not visited, start a new DFS
                dfs(i, compId, adjacencyList, visited, component);
                compId++; // Increment the component ID
            }
        }

        // Sort vertices by their component IDs
        Integer[] permutation = new Integer[N];
        for (int i = 0; i < N; i++) {
            permutation[i] = i;
        }
        Arrays.sort(permutation, Comparator.comparingInt(a -> component[a]));

        // Form the transformed matrix based on the sorted order of vertices
        int[][] outputMatrix = new int[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                outputMatrix[i][j] = matrix[permutation[i]][permutation[j]];
            }
        }

        // Print detailed logs if enabled
        if (allLogs) {
            System.out.println("\nConnected Components:");
            for (int i = 0; i < N; i++) {
                System.out.printf("Vertex %d belongs to component %d\n", i, component[i]);
            }
        }

        // Print the order of vertices for block diagonal form
        System.out.print("\nOrder of vertices for block diagonal form: ");
        for (int idx : permutation) {
            System.out.print(idx + " ");
        }
        System.out.println();

        // Print the transformed matrix if detailed logs are enabled
        if (allLogs) {
            System.out.println("\nTransformed Matrix (Block Diagonal Form):");
            printMatrix(outputMatrix);
        }
    }

    public static void main(String[] args) throws Exception {
        boolean logs = false; // Set to true to enable detailed logs

        // List of matrix names to process
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

        int[][] matrix;

        long startTime;
        long endTime;
        long duration;

        // Process each matrix in the list
        for (String s : matrixList) {
            matrix = readMatrixFromJson("src/main/resources/test_matrix.json", s);

            // Print the original matrix if logs are enabled
            if (logs) {
                System.out.println("[" + s + "] Original Matrix:");
                printMatrix(matrix);
            } else {
                System.out.print("[" + s + "]");
            }

            startTime = System.nanoTime(); // Start timing the algorithm

            algorithm(matrix, logs); // Run the algorithm

            endTime = System.nanoTime(); // End timing the algorithm
            duration = (endTime - startTime) / 1000; // Calculate duration in microseconds

            System.out.println("Execution Time: " + duration + " μs"); // Print the execution time
        }
    }
}