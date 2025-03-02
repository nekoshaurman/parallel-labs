    package neko.lab2;

    import mpi.MPI;

    public class part1 {
        public static void main(String[] args) {
            MPI.Init(args);

            int rank = MPI.COMM_WORLD.Rank();

            int ai;
            ai = rank;
            int bi = (rank + 1) * 2;

            System.out.println("P" + rank + " (id=" + Thread.currentThread().getId()
                    + "): a" + rank + "=" + ai + ", b" + rank + "=" + bi);

            int[] received_a = new int[1];
            int[] received_b = new int[1];

            int send_a_to, send_b_to;
            int get_a_from, get_b_from;

            switch (rank) {
                case 0:
                    send_a_to = 2; // send to solve c2
                    send_b_to = 1; // send to solve c1
                    get_a_from = 1; // get from c1
                    get_b_from = 2; // get from c2
                    break;
                case 1:
                    send_a_to = 0;
                    send_b_to = 3;
                    get_a_from = 3;
                    get_b_from = 0;
                    break;
                case 2:
                    send_a_to = 3;
                    send_b_to = 0;
                    get_a_from = 0;
                    get_b_from = 3;
                    break;
                case 3:
                    send_a_to = 1;
                    send_b_to = 2;
                    get_a_from = 2;
                    get_b_from = 1;
                    break;
                default:
                    throw new IllegalStateException("idk count of ranks");
            }

            if (rank == 0 || rank == 3) {
                MPI.COMM_WORLD.Send(new int[]{ai}, 0, 1, MPI.INT, send_a_to, 99);
                MPI.COMM_WORLD.Send(new int[]{bi}, 0, 1, MPI.INT, send_b_to, 99);
                MPI.COMM_WORLD.Recv(received_a, 0, 1, MPI.INT, get_a_from, 99);
                MPI.COMM_WORLD.Recv(received_b, 0, 1, MPI.INT, get_b_from, 99);
            } else {
                MPI.COMM_WORLD.Recv(received_a, 0, 1, MPI.INT, get_a_from, 99);
                MPI.COMM_WORLD.Recv(received_b, 0, 1, MPI.INT, get_b_from, 99);
                MPI.COMM_WORLD.Send(new int[]{ai}, 0, 1, MPI.INT, send_a_to, 99);
                MPI.COMM_WORLD.Send(new int[]{bi}, 0, 1, MPI.INT, send_b_to, 99);
            }

            System.out.println("P" + rank + " (id=" + Thread.currentThread().getId()
                    + "): c" + rank + "=" + received_a[0] + "+" + received_b[0] + "=" + (received_a[0] + received_b[0]));

            MPI.Finalize();
        }
    }