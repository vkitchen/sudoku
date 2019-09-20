import java.io.FileNotFoundException;
import java.io.File;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Sudoku Solver implementing Algorithm X https://en.wikipedia.org/wiki/Algorithm_X
 * Currently only useful when there is a single solution
 *
 * @author Vaughan Kitchen
 * @version 0.1, 25 Apr 2015
 */

class Node {
    public Node L, R, U, D, C; // Left, Right, Up, Down, Column Header
    public int S, N; // Size, Name (Header object use only)
}

// Insert left to right, top to bottom ONLY
class LinkedList {
    private Node root = new Node();
    private Node rowHead;

    public LinkedList(int width) {
        Node ref;
        root.L = root.R = root; // Make circular

        for (int i=0; i<width; i++) {
            ref = new Node();
            ref.N = i; // Name
            ref.L = root.L; // Last
            ref.R = root; // First
            ref.L.R = ref; // Last.R
            root.L = ref; // First.L
            ref.C = ref.U = ref.D = ref; // Make circular
        }
    }

    // First column numbered 0
    public void insert(int column) {
        Node header = root;
        Node ref = new Node();

        // Find the column
        for (int i=0; i<column+1; i++) header = header.R;

        header.S++; // Increase size
        ref.C = header; // Column Header
        ref.U = header.U; // Last
        ref.D = header; // First
        ref.U.D = ref; // Last.D
        header.U = ref; //First.U

        ref.L = ref.R = ref; // Make circular
        if (rowHead == null) rowHead = ref;
        ref.L = rowHead.L; // Last
        ref.R = rowHead; // First
        ref.L.R = ref; // Last.R
        rowHead.L = ref; // First.L
    }

    public void closeRow() {
        rowHead = null;
    }

    public Node getColumn() {
        Node shortest = (root.R != root) ? root.R : null; // Null, when solved
        for (Node ref : R(root, 1)) {
            if (ref.S < shortest.S) shortest = ref;
        }
        return shortest;
    }

    // Helper functions ////////////////////////////////////////////////////////
    // Iterate Left
    private Iterable<Node> L(Node item, int offset) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    Node ref = (offset == 0) ? item.R : item; // It's always 0|1
                    int count = offset;

                    @Override
                    public boolean hasNext() {
                        return (ref.L != item || count == 0) ? true : false;
                    }

                    @Override
                    public Node next() {
                        ref = ref.L;
                        count++;
                        return ref;
                    }
                };
            }
        };
    }

    // Iterate Right
    private Iterable<Node> R(Node item, int offset) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    Node ref = (offset == 0) ? item.L : item; // It's always 0|1
                    int count = offset;

                    @Override
                    public boolean hasNext() {
                        return (ref.R != item || count == 0) ? true : false;
                    }

                    @Override
                    public Node next() {
                        ref = ref.R;
                        count++;
                        return ref;
                    }
                };
            }
        };
    }

    // Skip the headers
    private Node nextUp(Node ref) {
        return (ref.U != ref.C) ? ref.U : ref.U.U;
    }
        
    private Node nextDown(Node ref) {
        return (ref.D != ref.C) ? ref.D : ref.D.D;
    }

    // Iterate Up
    private Iterable<Node> U(Node item, int offset) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    Node ref = (offset == 0) ? nextDown(item) : item; // It's always 0|1
                    int count = offset;

                    @Override
                    public boolean hasNext() {
                        return (nextUp(ref) != item || count == 0) ? true : false;
                    }

                    @Override
                    public Node next() {
                        ref = nextUp(ref);
                        count++;
                        return ref;
                    }
                };
            }
        };
    }

    // Iterate Down
    private Iterable<Node> D(Node item, int offset) {
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    Node ref = (offset == 0) ? nextUp(item) : item; // It's always 0|1
                    int count = offset;

                    @Override
                    public boolean hasNext() {
                        return (nextDown(ref) != item || count == 0) ? true : false;
                    }

                    @Override
                    public Node next() {
                        ref = nextDown(ref);
                        count++;
                        return ref;
                    }
                };
            }
        };
    }
    // END /////////////////////////////////////////////////////////////////////
    public void hide(Node ref) {
        for (Node column : R(ref, 0)) {
            // Remove header
            column.C.L.R = column.C.R;
            column.C.R.L = column.C.L;
            for (Node row : D(column, 1)) {
                for (Node item : R(row, 1)) {
                    // Remove item
                    item.C.S--;
                    item.U.D = item.D;
                    item.D.U = item.U;
                }
            }
        }
    }

    public void restore(Node ref) {
        // Opposite order from hide()
        for (Node column : L(ref.L, 0)) {
            for (Node row : U(column, 1)) {
                for (Node item : L(row, 1)) {
                    // Restore item
                    item.C.S++;
                    item.U.D = item;
                    item.D.U = item;
                }
            }
            // Restore header
            column.C.L.R = column.C;
            column.C.R.L = column.C;
        }
    }

    int[][] solution = new int[9][9];
    // Take the red pill
    private void releaseLine(Node ref) {
        int[] line = new int[4];
        for (Node item : R(ref, 0)) line[(item.C.N) / 81] = item.C.N; // Sort the line
        int row = (line[0]) / 9; // cell: increases by col, row
        int col = (line[0]) % 9; // cell: increases by col, row
        int val = line[1] - 80 - 9 * row; // row: increases by cell, row
        solution[col][row] = val;
    }

    // Pretty print the solution
    private void printSolution() {
        System.out.println(" -----------------------");
        for (int y=0; y<solution.length; y++) {
            System.out.print("| ");
            for (int x=0; x<solution[y].length; x++) {
                System.out.print(solution[x][y] + " ");
                if ((x + 1) % 3 == 0) System.out.print("| ");
            }
            System.out.print("\n");
            if ((y + 1) % 3 == 0) System.out.println(" -----------------------");
        }
    }

    private void curse(Node ref) {
        releaseLine(ref); // Add to the partial solution
        hide(ref);
        solve();
        restore(ref);
        if (nextDown(ref) != ref) curse(nextDown(ref));
    }

    public void solve() {
        Node col = getColumn();
        if (col == null) printSolution();
        else if (col.S != 0) curse(nextDown(col));
    }
}

public class DLX {
    private static LinkedList matrix;

    private static void insert(int col, int row, int val) {
        // col, row, val, reg = {0..8}
        // matrix has 4 constraint sets 81 cols wide
        matrix.insert(row * 9 + col); // Cell
        matrix.insert(row * 9 + val + 81); // Row
        matrix.insert(col * 9 + val + 162); // Col
        matrix.insert((row / 3 * 3 + col / 3) * 9 + val + 243); // Region
        matrix.closeRow();
    }

    private static void read(File file) {
        matrix = new LinkedList(324);
        try {
            Scanner scan = new Scanner(file);
            for (int y=0; scan.hasNextLine(); y++) {
                String line = scan.nextLine();
                for (int x=0; x<line.length(); x++) {
                    int v = Character.getNumericValue(line.charAt(x));
                    if (v != 0) insert(x, y, v - 1);
                    else for (int i=0; i<9; i++) insert(x, y, i);
                }
            }
        } catch (FileNotFoundException er) {
            er.printStackTrace();
        }
    }

    public static void main(String[] argv) {
        if (argv.length > 0) {
            read(new File(argv[0]));
            matrix.solve();
        } else {
            System.err.println("No file provided");
            System.exit(1);
        }
    }
}
