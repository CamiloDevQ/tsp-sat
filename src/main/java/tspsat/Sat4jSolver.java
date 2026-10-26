package tspsat;

import java.nio.file.Path;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.DimacsReader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;

/** SolverSAT implementado con Sat4j. */
public final class Sat4jSolver implements SolverSAT {
    @Override
    public int[] resolver(Path dimacs) throws Exception {
        DimacsReader lector = new DimacsReader(SolverFactory.newDefault());
        try {
            IProblem problema = lector.parseInstance(dimacs.toString());
            return problema.isSatisfiable() ? problema.model() : null;
        } catch (ContradictionException e) {
            return null;
        }
    }
}
