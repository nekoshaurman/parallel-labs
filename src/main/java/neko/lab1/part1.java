package neko.lab1;

import mpi.MPI;

public class part1 {
    public static void main(String[] args) {
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        for (int i = rank + 2; i <= 10; i += size) {
            for (int j = 2; j <= 10; j++) {
                System.out.println("P" + rank + " (id=" + Thread.currentThread().getId()
                        + "): " + i + "*" + j + "=" + (i * j));
            }
        }

        MPI.Finalize();
    }
}
