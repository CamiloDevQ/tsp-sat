package tspsat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Reducción TSP(decisión) -> SAT en formato DIMACS CNF. Solo traduce, no resuelve.
 * Variables: X(i,t) = i*N + t + 1 (ciudad i en el paso t);
 *            C(s,v) = N*N + (s-1)*K + v (el costo de las primeras s transiciones es >= v).
 */
public final class GeneradorCNF {
    private final StringBuilder cuerpo = new StringBuilder();
    private long clausulas = 0;
    private long variables;
    private int n, k;

    public long clausulas() { return clausulas; }
    public long variables() { return variables; }

    private int x(int i, int t) { return i * n + t + 1; }
    private int c(int s, int v) { return n * n + (s - 1) * k + v; }

    private void cl(int... lits) {
        for (int l : lits) cuerpo.append(l).append(' ');
        cuerpo.append("0\n");
        clausulas++;
    }

    /** Genera el archivo DIMACS y retorna n (para decodificar X(i,t)). */
    public void generar(Grafo g, Path salida) throws IOException {
        n = g.n;
        int maxD = 0;
        for (int[] fila : g.dist) for (int d : fila) maxD = Math.max(maxD, d);
        boolean costoVacuo = n == 1 || g.k >= (long) n * maxD;   // ningún ciclo puede superar K
        k = costoVacuo ? 0 : (int) Math.max(g.k, 0);
        variables = (long) n * n + (costoVacuo ? 0 : (long) (n - 1) * k);

        if (g.k < 0) { cl(1); cl(-1); }                          // K negativo: insatisfacible
        cl(x(0, 0));                                             // ciudad 0 en el paso 0 (simetría)
        for (int a = 0; a < n; a++) {
            int[] porCiudad = new int[n], porPaso = new int[n];
            for (int b = 0; b < n; b++) { porCiudad[b] = x(a, b); porPaso[b] = x(b, a); }
            cl(porCiudad);                                       // 1) ciudad a visitada alguna vez
            cl(porPaso);                                         // 2) paso a ocupado por alguna ciudad
            for (int b1 = 0; b1 < n; b1++)
                for (int b2 = b1 + 1; b2 < n; b2++) {
                    cl(-x(a, b1), -x(a, b2));                    // 1) a lo sumo un paso por ciudad
                    cl(-x(b1, a), -x(b2, a));                    // 2) a lo sumo una ciudad por paso
                }
        }
        if (!costoVacuo) costo(g);

        StringBuilder enc = new StringBuilder("p cnf " + variables + " " + clausulas + "\n");
        Files.writeString(salida, enc.append(cuerpo));
    }

    /** 3) Costo total (ciclo cerrado) <= K con contador unario. */
    private void costo(Grafo g) {
        for (int t = 0; t < n; t++) {
            int tn = (t + 1) % n;
            boolean ultima = t == n - 1;
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++) {
                    if (i == j) continue;
                    int d = g.dist[i][j], a = -x(i, t), b = -x(j, tn);
                    if (d > k) { cl(a, b); continue; }           // arista que sola supera K
                    if (d >= 1 && !ultima) cl(a, b, c(t + 1, d));
                    if (t >= 1)
                        for (int v = 1; v <= k; v++) {
                            if (v + d > k) cl(a, b, -c(t, v));   // se superaría K
                            else if (!ultima) cl(a, b, -c(t, v), c(t + 1, v + d));
                        }
                }
        }
    }
}
