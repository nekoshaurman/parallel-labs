package neko.lab2;

import mpi.MPI;

public class part2 {

    public static void main(String[] args) {
        MPI.Init(args);

        int np = MPI.COMM_WORLD.Size();
        int rank = MPI.COMM_WORLD.Rank();

        // Get vector from command
        String vectorString = System.getProperty("vector", "1 2 3 4 5");
        String[] elements = vectorString.split("\\s+");
        int[] vector = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            vector[i] = Integer.parseInt(elements[i]);
        }

        // Size of input vector
        int size = vector.length;

        // Resolve size of local vectors
        int blockSize = size / np;
        int remainder = size % np;

        // Size of local vectors
        int localSize = blockSize + (rank == np - 1 ? remainder : 0);
        int[] localVector = new int[localSize];

        if (rank == 0) {
            // P0 send parts of vector
            int offset = 0;
            for (int i = 0; i < np; i++) {
                int count = blockSize + (i == np - 1 ? remainder : 0);
                if (i == 0) {
                    // P0 get local vector
                    System.arraycopy(vector, offset, localVector, 0, count);
                } else {
                    // Send vectors to other P's
                    MPI.COMM_WORLD.Send(vector, offset, count, MPI.INT, i, 0);
                }
                offset += count;
            }
        } else {
            // Other P's get local vector
            MPI.COMM_WORLD.Recv(localVector, 0, localSize, MPI.INT, 0, 0);
        }

        // Sum inside P
        int localSum = 0;
        for (int j : localVector) {
            if (j % 2 != 0) {
                localSum += j;
            }
        }

        // Get sum from other P
        int[] globalSums = new int[np];
        MPI.COMM_WORLD.Gather(new int[]{localSum}, 0, 1, MPI.INT, globalSums, 0, 1, MPI.INT, 0);

        // Sum in P0
        if (rank == 0) {
            int totalSum = 0;
            for (int sum : globalSums) {
                totalSum += sum;
            }
            System.out.println("Sum: " + totalSum);
        }

        MPI.Finalize();
    }
}