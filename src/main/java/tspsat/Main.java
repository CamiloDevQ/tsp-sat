package tspsat;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Uso: Main <instancia.txt> [K]  — lee el grafo, genera salida.cnf, resuelve con Sat4j y reconstruye la ruta. */
public final class Main {
    public static void main(String[] args) throws Exception {
        Grafo g = Grafo.leer(Paths.get(args[0]));
        if (args.length > 1) g = g.conK(Long.parseLong(args[1]));

        Path cnf = Paths.get("salida.cnf");
        SolverSAT solver = new Sat4jSolver();

        long t0 = System.nanoTime();
        GeneradorCNF gen = new GeneradorCNF();
        gen.generar(g, cnf);
        long t1 = System.nanoTime();
        System.out.printf("N=%d K=%d | variables=%d clausulas=%d | generacion=%.1f ms%n",
                g.n, g.k, gen.variables(), gen.clausulas(), (t1 - t0) / 1e6);

        int[] modelo = solver.resolver(cnf);
        System.out.printf("solver=%.1f ms%n", (System.nanoTime() - t1) / 1e6);

        if (modelo == null) {
            System.out.println("NO existe ninguna ruta con costo menor o igual a K");
            return;
        }
        int[] ruta = new int[g.n];
        for (int lit : modelo)
            if (lit > 0 && lit <= g.n * g.n) ruta[(lit - 1) % g.n] = (lit - 1) / g.n;
        StringBuilder sb = new StringBuilder();
        for (int c : ruta) sb.append(c).append(" -> ");
        System.out.println("Ruta: " + sb + ruta[0]);
        System.out.println("Costo real: " + g.costo(ruta));
    }
}
