package neko.lab4;

import mpi.MPI;

public class part1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int np = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        // Get vector
        String vectorString = System.getProperty("vector", "1 2 3 4 5");
        String[] elements = vectorString.split("\\s+");
        int[] vector = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            vector[i] = Integer.parseInt(elements[i]);
        }

        // Size of vector
        int size = vector.length;
        int blockSize = size / np;
        int remainder = size % np;
        int[] counts = new int[np];
        int[] displacements = new int[np];

        // Определяем размеры блоков для каждого процесса
        for (int i = 0; i < np; i++) {
            counts[i] = blockSize + (i < remainder ? 1 : 0);
            displacements[i] = (i == 0) ? 0 : displacements[i - 1] + counts[i - 1];
        }

        // Local vector to P's
        int localSize = counts[rank];
        int[] localVector = new int[localSize];

        // Allocation data to P's
        MPI.COMM_WORLD.Scatterv(vector, 0, counts, displacements, MPI.INT, localVector, 0, localSize, MPI.INT, 0);

        // Sum inside P
        int localSum = 0;
        for (int value : localVector) {
            if (value % 2 != 0) {
                localSum += value;
            }
        }

        // Get sum by reduce
        int[] totalSum = new int[1];
        MPI.COMM_WORLD.Reduce(new int[]{localSum}, 0, totalSum, 0, 1, MPI.INT, MPI.SUM, 0);

        // Output from P0
        if (rank == 0) {
            System.out.println("Sum: " + totalSum[0]);
        }

        MPI.Finalize();
    }
}
