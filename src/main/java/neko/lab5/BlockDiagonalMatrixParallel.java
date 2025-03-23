package neko.lab5;

import mpi.*;
import java.util.*;

public class BlockDiagonalMatrixParallel {
    public static void main(String[] args) throws Exception {
        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int[][] matrix = {
                {0, 0, 33, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 44, 0, 0, 0, 0, 0, 0},
                {55, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 96, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 77, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 77, 0},
                {0, 0, 0, 0, 0, 0, 0, 88, 0, 0},
                {0, 0, 11, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 22, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 77, 0, 0, 0}
        };

        if (rank == 0) {
            System.out.println("Исходная матрица:");
            printMatrix(matrix);
        }

        long startTime = System.nanoTime();
        matrix = transformToBlockDiagonal(matrix, rank, size);
        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        if (rank == 0) {
            System.out.println("Матрица после преобразования:");
            printMatrix(matrix);
            System.out.println("Время выполнения: " + duration + " наносекунд");
        }

        MPI.Finalize();
    }

    public static int[][] transformToBlockDiagonal(int[][] matrix, int rank, int size) {
        int n = matrix.length;
        boolean[] rowMarked = new boolean[n];
        boolean[] colMarked = new boolean[n];
        int[] rowLabels = new int[n];
        int[] colLabels = new int[n];
        Arrays.fill(rowLabels, -1);
        Arrays.fill(colLabels, -1);
        int label = 1;

        // Каждый процесс маркирует свою часть строк
        for (int i = rank; i < n; i += size) {
            if (rowLabels[i] == -1) {
                System.out.println("Процесс " + rank + " маркирует компонент связности с меткой " + label);
                labelRowsAndCols(matrix, i, label, rowLabels, colLabels, rowMarked, colMarked, rank);
                label++;
            }
        }

        MPI.COMM_WORLD.Barrier();

        // Сбор всех меток строк и столбцов со всех процессов
        int[] gatheredRowLabels = new int[n * size];
        int[] gatheredColLabels = new int[n * size];

        MPI.COMM_WORLD.Allgather(rowLabels, 0, n, MPI.INT, gatheredRowLabels, 0, n, MPI.INT);
        MPI.COMM_WORLD.Allgather(colLabels, 0, n, MPI.INT, gatheredColLabels, 0, n, MPI.INT);

        // Объединяем результаты из всех процессов
        for (int i = 0; i < n; i++) {
            for (int p = 0; p < size; p++) {
                if (gatheredRowLabels[p * n + i] != -1) {
                    rowLabels[i] = gatheredRowLabels[p * n + i];
                }
                if (gatheredColLabels[p * n + i] != -1) {
                    colLabels[i] = gatheredColLabels[p * n + i];
                }
            }
        }

        MPI.COMM_WORLD.Barrier();

        if (rank == 0) {
            matrix = reorderMatrix(matrix, rowLabels, colLabels);
        }

        return matrix;
    }

    private static void labelRowsAndCols(int[][] matrix, int row, int label, int[] rowLabels, int[] colLabels, boolean[] rowMarked, boolean[] colMarked, int rank) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(row);
        rowLabels[row] = label;
        rowMarked[row] = true;
        System.out.println("  [" + rank + "] Строка " + row + " получает метку " + label);

        while (!queue.isEmpty()) {
            int r = queue.poll();
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[r][j] != 0 && !colMarked[j]) {
                    colLabels[j] = label;
                    colMarked[j] = true;
                    System.out.println("  [" + rank + "] Столбец " + j + " получает метку " + label);
                    for (int i = 0; i < matrix.length; i++) {
                        if (matrix[i][j] != 0 && !rowMarked[i]) {
                            rowLabels[i] = label;
                            rowMarked[i] = true;
                            queue.add(i);
                            System.out.println("  [" + rank + "] Строка " + i + " получает метку " + label);
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

        Arrays.sort(rowOrder, Comparator.comparingInt(i -> rowLabels[i]));
        Arrays.sort(colOrder, Comparator.comparingInt(i -> colLabels[i]));

        int[][] newMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                newMatrix[i][j] = matrix[rowOrder[i]][colOrder[j]];
            }
        }

        System.out.println("Новый порядок строк: " + Arrays.toString(rowOrder));
        System.out.println("Новый порядок столбцов: " + Arrays.toString(colOrder));

        return newMatrix;
    }

    private static void printMatrix(int[][] matrix) {
        for (int[] row : matrix) {
            System.out.println(Arrays.toString(row));
        }
        System.out.println();
    }
}
