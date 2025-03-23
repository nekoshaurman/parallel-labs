package neko.lab5;

import mpi.*;
import java.util.*;

public class Parallel {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int n = 5;
        int[][] matrix = new int[n][n];

        if (rank == 0) {
            // Исходная разреженная матрица
            matrix = new int[][]{
                    {0, 0, 3, 0, 0},
                    {0, 0, 0, 4, 0},
                    {5, 0, 0, 0, 0},
                    {0, 6, 0, 0, 0},
                    {0, 0, 0, 0, 7}
            };

            System.out.println("Исходная матрица:");
            printMatrix(matrix);
        }

        // Рассылаем матрицу всем процессам
        for (int i = 0; i < n; i++) {
            MPI.COMM_WORLD.Bcast(matrix[i], 0, n, MPI.INT, 0);
        }

        long startTime = System.nanoTime();
        matrix = transformToBlockDiagonal(matrix, rank, size, n);
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        if (rank == 0) {
            System.out.println("Матрица после преобразования:");
            printMatrix(matrix);
            System.out.println("Время выполнения: " + duration + " наносекунд");
        }

        MPI.Finalize();
    }

    public static int[][] transformToBlockDiagonal(int[][] matrix, int rank, int size, int n) {
        int rowsPerProcess = n / size;
        int extraRows = n % size;
        int startRow = rank * rowsPerProcess + Math.min(rank, extraRows);
        int endRow = startRow + rowsPerProcess + (rank < extraRows ? 1 : 0);

        int[] rowLabels = new int[n];
        int[] colLabels = new int[n];
        Arrays.fill(rowLabels, -1);
        Arrays.fill(colLabels, -1);

        boolean[] rowMarked = new boolean[n];
        boolean[] colMarked = new boolean[n];

        int[] labelCounter = new int[1];
        if (rank == 0) labelCounter[0] = 1;

        MPI.COMM_WORLD.Bcast(labelCounter, 0, 1, MPI.INT, 0);

        for (int i = startRow; i < endRow; i++) {
            if (rowLabels[i] == -1) {
                labelRowsAndCols(matrix, i, rowLabels, colLabels, rowMarked, colMarked, n, labelCounter);
                labelCounter[0]++;
            }
        }

        MPI.COMM_WORLD.Barrier();
        MPI.COMM_WORLD.Allreduce(rowLabels, 0, rowLabels, 0, n, MPI.INT, MPI.SUM);
        MPI.COMM_WORLD.Allreduce(colLabels, 0, colLabels, 0, n, MPI.INT, MPI.SUM);

        if (rank == 0) {
            matrix = reorderMatrix(matrix, rowLabels, colLabels);
        }

        return matrix;
    }

    private static void labelRowsAndCols(int[][] matrix, int row, int[] rowLabels, int[] colLabels, boolean[] rowMarked, boolean[] colMarked, int n, int[] labelCounter) {
        Queue<Integer> queue = new LinkedList<>();
        int label = labelCounter[0];

        queue.add(row);
        rowLabels[row] = label;
        rowMarked[row] = true;

        while (!queue.isEmpty()) {
            int r = queue.poll();
            for (int j = 0; j < n; j++) {
                if (matrix[r][j] != 0 && !colMarked[j]) {
                    colLabels[j] = label;
                    colMarked[j] = true;
                    for (int i = 0; i < n; i++) {
                        if (matrix[i][j] != 0 && !rowMarked[i]) {
                            rowLabels[i] = label;
                            rowMarked[i] = true;
                            queue.add(i);
                        }
                    }
                }
            }
        }
    }

    private static int[][] reorderMatrix(int[][] matrix, int[] rowLabels, int[] colLabels) {
        int n = matrix.length;
        Integer[] rowOrder = new Integer[n];
        Integer[] colOrder = new Integer[n];

        for (int i = 0; i < n; i++) {
            rowOrder[i] = i;
            colOrder[i] = i;
        }

        // Сортируем строки и столбцы по меткам
        Arrays.sort(rowOrder, Comparator.comparingInt(i -> rowLabels[i]));
        Arrays.sort(colOrder, Comparator.comparingInt(i -> colLabels[i]));

        int[][] newMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newMatrix[i][j] = matrix[rowOrder[i]][colOrder[j]];
            }
        }

        return newMatrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
    }
}
