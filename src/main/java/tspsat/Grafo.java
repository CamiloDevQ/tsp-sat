package tspsat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Instancia TSP (decisión): N, matriz de distancias y umbral K. */
public final class Grafo {
    public final int n;
    public final int[][] dist;
    public final long k;

    public Grafo(int[][] dist, long k) {
        this.n = dist.length;
        this.dist = dist;
        this.k = k;
    }

    /** Formato: "N K" y luego N filas con N distancias. Líneas con '#' se ignoran. */
    public static Grafo leer(Path archivo) throws IOException {
        List<String> tok = new ArrayList<>();
        for (String linea : Files.readAllLines(archivo)) {
            linea = linea.trim();
            if (linea.isEmpty() || linea.startsWith("#")) continue;
            for (String t : linea.split("\\s+")) tok.add(t);
        }
        int n = Integer.parseInt(tok.get(0));
        long k = Long.parseLong(tok.get(1));
        int[][] d = new int[n][n];
        int p = 2;
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++) d[i][j] = Integer.parseInt(tok.get(p++));
        return new Grafo(d, k);
    }

    public Grafo conK(long nuevoK) { return new Grafo(dist, nuevoK); }

    public long costo(int[] ruta) {
        long c = 0;
        for (int t = 0; t < n; t++) c += dist[ruta[t]][ruta[(t + 1) % n]];
        return c;
    }
}
