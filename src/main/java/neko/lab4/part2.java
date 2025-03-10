package neko.lab4;

import mpi.MPI;

public class part2 {
    public static void main(String[] args) {
        MPI.Init(args);

        int np = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        // Input or set matrix
        int[][] matrix = null;
        int rows = 0, cols = 0;

        if (rank == 0) {
            String matrixString = System.getProperty("matrix", "-1 2 -3 4;5 -6 7 -8;-9 10 -11 12");
            String[] matrixRows = matrixString.split(";");
            rows = matrixRows.length;
            cols = matrixRows[0].split("\\s+").length;
            matrix = new int[rows][cols];
            for (int i = 0; i < rows; i++) {
                String[] elements = matrixRows[i].split("\\s+");
                for (int j = 0; j < cols; j++) {
                    matrix[i][j] = Integer.parseInt(elements[j]);
                }
            }
        }

        // Size of matrix
        int[] sizeInfo = new int[2];
        if (rank == 0) {
            sizeInfo[0] = rows;
            sizeInfo[1] = cols;
        }

        // Share size of metrix
        MPI.COMM_WORLD.Bcast(sizeInfo, 0, 2, MPI.INT, 0);
        rows = sizeInfo[0];
        cols = sizeInfo[1];

        // Resolve size of local matrix
        int blockSize = cols / np;
        int remainder = cols % np;
        int[] counts = new int[np];
        int[] displacements = new int[np];

        for (int i = 0; i < np; i++) {
            counts[i] = (blockSize + (i < remainder ? 1 : 0)) * rows;
            displacements[i] = (i == 0) ? 0 : displacements[i - 1] + counts[i - 1];
        }

        // Size of local columns
        int localCols = counts[rank] / rows;
        int[] localMatrix = new int[rows * localCols];
        int[] flattenedMatrix = null;

        if (rank == 0) {
            flattenedMatrix = new int[rows * cols];
            for (int r = 0; r < rows; r++) {
                System.arraycopy(matrix[r], 0, flattenedMatrix, r * cols, cols);
            }
        }

        // Allocation data to P's
        MPI.COMM_WORLD.Scatterv(flattenedMatrix, 0, counts, displacements, MPI.INT, localMatrix, 0, localMatrix.length, MPI.INT, 0);

        // Counting unchanged numbers
        int unchangedCount = 0;

        // Change numbers to 1, -1 and 0
        for (int i = 0; i < localMatrix.length; i++) {
            if (localMatrix[i] > 0) {
                if (localMatrix[i] == 1) {
                    unchangedCount++;
                }
                else {
                    localMatrix[i] = 1;
                }
            } else if (localMatrix[i] < 0) {
                if (localMatrix[i] == -1) {
                    unchangedCount++;
                }
                else {
                    localMatrix[i] = -1;
                }
            } else {
                {
                    unchangedCount++;
                }
            }
        }

        // Result matrix
        int[] resultMatrix = null;
        if (rank == 0) {
            resultMatrix = new int[rows * cols];
        }

        // Get local data from P's
        MPI.COMM_WORLD.Gatherv(localMatrix, 0, localMatrix.length, MPI.INT, resultMatrix, 0, counts, displacements, MPI.INT, 0);

        // Count unchanged elements
        int[] totalUnchanged = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{unchangedCount}, 0, totalUnchanged, 0, 1, MPI.INT, MPI.SUM, 0);

        // Results
        if (rank == 0) {
            System.out.println("Result Matrix:");
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    System.out.print(resultMatrix[r * cols + c] + " ");
                }
                System.out.println();
            }
            System.out.println("Total unchanged elements: " + totalUnchanged[0]);
        }

        MPI.Finalize();
    }
}
