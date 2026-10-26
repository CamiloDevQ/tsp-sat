# TSP ≤p SAT (Java 11, Sat4j)

Reducción polinomial del **Problema del Viajante (versión de decisión)** a **SAT**.

Dado un grafo completo de N nodos con matriz de distancias y un umbral K, el programa responde: *¿existe un ciclo que visite todas las ciudades exactamente una vez, regrese al origen y tenga costo ≤ K?*

El código Java **solo traduce y lee resultados**. Quien resuelve el problema es el SAT-solver **Sat4j**; no hay fuerza bruta, programación dinámica ni metaheurísticas.

Autor: Camilo Quirola

## Flujo del programa

1. **Fase 1 – Reducción:** `GeneradorCNF` lee el grafo y escribe `salida.cnf` en formato DIMACS.
2. **Fase 2 – Solver:** `Sat4jSolver` carga el CNF y lo resuelve con Sat4j.
3. **Fase 3 – Reconstrucción:**
   - Si es UNSAT, imprime `NO existe ninguna ruta con costo menor o igual a K`.
   - Si es SAT, lee el modelo, reconstruye la ruta y calcula su costo real sobre la matriz.

## Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 11 o superior |
| Maven | 3.6 o superior (o NetBeans con Maven integrado) |
| Sat4j | `org.ow2.sat4j.core` 2.3.6, se descarga automáticamente desde `pom.xml` |

Dependencia declarada en `pom.xml`:

```xml
<dependency>
  <groupId>org.ow2.sat4j</groupId>
  <artifactId>org.ow2.sat4j.core</artifactId>
  <version>2.3.6</version>
</dependency>
```

La primera compilación necesita conexión a internet para bajar Sat4j.

## Estructura del proyecto

```
tsp-sat/
├── pom.xml
├── README.md
├── instancias/            # archivos de entrada (n3.txt, n4.txt, ...)
├── salida.cnf             # CNF generado en la última ejecución
└── src/main/java/tspsat/
    ├── Main.java          # orquesta las 3 fases
    ├── Grafo.java         # lectura del grafo y cálculo del costo real
    ├── GeneradorCNF.java  # reducción TSP -> CNF (DIMACS)
    ├── SolverSAT.java     # interfaz del solver
    └── Sat4jSolver.java   # implementación con Sat4j
```

`SolverSAT` desacopla la reducción del solver concreto: se podría cambiar Sat4j por otro sin tocar el generador.

## Compilar

Por consola, desde la carpeta `tsp-sat`:

```
mvn clean compile
```

En NetBeans: *File → Open Project*, elegir la carpeta `tsp-sat` y luego *Clean and Build* (Shift+F11).

## Ejecutar

Argumentos: `<archivo_instancia> [K]`. El segundo argumento es opcional y reemplaza el K del archivo.

Por consola:

```
mvn -q compile exec:java -Dexec.args="instancias/n4.txt"
mvn -q compile exec:java -Dexec.args="instancias/n15.txt 27"
```

En NetBeans: *Project Properties → Run → Arguments* con `instancias/n4.txt` (o `instancias/n15.txt 27`) y luego *Run* (F6).

## Formato de entrada

```
N K
d[0][0] ... d[0][N-1]
...
d[N-1][0] ... d[N-1][N-1]
```

Las líneas que empiezan con `#` son comentarios. Las distancias son enteros no negativos. Ejemplo (N = 3, K = 21):

```
3 21
0 5 9
5 0 7
9 7 0
```

## Ejemplo de salida

Caso SAT (`instancias/n3.txt`):

```
N=3 K=21 | variables=51 clausulas=205 | generacion=9,1 ms
solver=67,5 ms
Ruta: 0 -> 2 -> 1 -> 0
Costo real: 21
```

Caso UNSAT (`instancias/n4_unsat.txt`):

```
N=4 K=79 | variables=253 clausulas=2259 | generacion=10,6 ms
solver=65,9 ms
NO existe ninguna ruta con costo menor o igual a K
```

Cada ejecución imprime el número de variables, el número de cláusulas, el tiempo de generación y el tiempo del solver, y deja el CNF en `salida.cnf`.

## Instancias incluidas

| Archivo | N | K | Resultado esperado |
|---|---|---|---|
| `n3.txt` | 3 | 21 | SAT |
| `n4.txt` | 4 | 80 | SAT |
| `n4_unsat.txt` | 4 | 79 | UNSAT |
| `n10.txt` | 10 | 50 | SAT |
| `n12.txt` | 12 | 60 | SAT |
| `n15.txt` | 15 | 75 | SAT (con K = 27 es SAT y con K = 26 es UNSAT) |
| `n20.txt`, `n25.txt`, `n30.txt`, `n35.txt` | 20 a 35 | 5·N | SAT |

## Reproducir las pruebas del informe

Ejecutar cada instancia de la tabla anterior y anotar variables, cláusulas, tiempo de generación y tiempo del solver.

Para el barrido de K en N = 15, ejecutar `n15.txt` con K = 75, 69, 63, 55, 45, 27 y 26:

```
mvn -q compile exec:java -Dexec.args="instancias/n15.txt 27"
```

Con K = 27 el solver encuentra una ruta (cerca del umbral tarda varios segundos) y con K = 26 demuestra que no existe ninguna (UNSAT, el caso más lento).

## Resumen de la reducción

**Variables**

- `x(i,t)`: la ciudad `i` se visita en el paso `t` (N² variables).
- `s(t,c)`: el costo acumulado tras `t` transiciones es al menos `c` (contador unario, (N−1)·K variables).

**Restricciones**

1. **Visita única:** cada ciudad aparece en exactamente un paso.
2. **Ocupación única:** en cada paso hay exactamente una ciudad.
3. **Costo:** el contador acumula las distancias de cada transición, incluido el regreso al origen, y se prohíbe superar K.

La ciudad 0 se fija en el paso 0 para evitar rotaciones equivalentes.

**Tamaño de la fórmula:** variables = N² + (N−1)·K y cláusulas = Θ(N³·K), es decir O(N⁴) cuando K crece linealmente con N. El detalle matemático, la demostración de corrección y el análisis de complejidad están en el informe técnico.

## Notas

- El tiempo del solver depende de qué tan cerca esté K del costo óptimo: con K holgado se resuelve rápido, y cerca del óptimo (o en UNSAT) puede tardar mucho más aunque la fórmula sea más chica.
- La reducción es pseudo-polinomial en K, porque el contador es unario (ver informe).
