package tspsat;

import java.nio.file.Path;

/** Interfaz con el SAT-solver. */
public interface SolverSAT {
    /** Retorna el modelo (literales +v / -v) si es SAT, o null si es UNSAT. */
    int[] resolver(Path dimacs) throws Exception;
}
