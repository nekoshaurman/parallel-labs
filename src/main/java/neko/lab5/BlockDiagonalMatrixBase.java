package neko.lab5;

import java.util.*;

public class BlockDiagonalMatrixBase {
    public static void main(String[] args) {
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

        System.out.println("Исходная матрица:");
        printMatrix(matrix);

        long startTime = System.nanoTime();

        matrix = transformToBlockDiagonal(matrix);

        long endTime = System.nanoTime();
        long duration = endTime - startTime;

        System.out.println("Матрица после преобразования:");
        printMatrix(matrix);
        System.out.println("Время выполнения: " + duration + " наносекунд");
    }

    public static int[][] transformToBlockDiagonal(int[][] matrix) {
        int n = matrix.length;
        boolean[] rowMarked = new boolean[n];
        boolean[] colMarked = new boolean[n];
        int[] rowLabels = new int[n];
        int[] colLabels = new int[n];
        Arrays.fill(rowLabels, -1);
        Arrays.fill(colLabels, -1);
        int label = 1;

        for (int i = 0; i < n; i++) {
            if (rowLabels[i] == -1) {
                System.out.println("Маркировка компонента связности с меткой " + label);
                labelRowsAndCols(matrix, i, label, rowLabels, colLabels, rowMarked, colMarked);
                label++;
            }
        }

        System.out.println("Перестановка строк и столбцов");
        matrix = reorderMatrix(matrix, rowLabels, colLabels);
        return matrix;
    }

    private static void labelRowsAndCols(int[][] matrix, int row, int label, int[] rowLabels, int[] colLabels, boolean[] rowMarked, boolean[] colMarked) {
        Queue<Integer> queue = new LinkedList<>();
        queue.add(row);
        rowLabels[row] = label;
        rowMarked[row] = true;
        System.out.println("  Строка " + row + " получает метку " + label);

        while (!queue.isEmpty()) {
            int r = queue.poll();
            for (int j = 0; j < matrix.length; j++) {
                if (matrix[r][j] != 0 && !colMarked[j]) {
                    colLabels[j] = label;
                    colMarked[j] = true;
                    System.out.println("  Столбец " + j + " получает метку " + label);
                    for (int i = 0; i < matrix.length; i++) {
                        if (matrix[i][j] != 0 && !rowMarked[i]) {
                            rowLabels[i] = label;
                            rowMarked[i] = true;
                            queue.add(i);
                            System.out.println("  Строка " + i + " получает метку " + label);
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
