import java.io.*;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.*;
import static java.lang.System.*;

public class Bonus {
    private static BufferedReader br;
    private static BufferedWriter bw;
    private static Scanner inp;
    static Classifier c;
    private static DataHolder dataHolder;
    private static File infile = null, outfile = null;

    // True Positive, False Positive, etc?
    enum Outcomes {
        TP(0,0), FP(0,1), FN(1,0), TN(1,1);
        final int byClassifier, trueValue;
        Outcomes( int b, int t ) {
            byClassifier = b;
            trueValue = t;
        }
        public static Outcomes which( int b, int t ) {
            for ( Outcomes o: values() )
                if ( o.byClassifier == b && o.trueValue == t )
                    return o;
            return null;
        }
    }

    private static final double percent = 0.70;

    public static void main(String... args) throws Exception {
        String filename = null;

        inp = new Scanner(System.in);

        out.printf("\n\n" + MyUtils.ASCII_BOLD+"Assignment05 Bonus Part: "+MyUtils.ANSI_RESET+MyUtils.ANSI_BLUE+ "NCB Classification tool\n\n"+MyUtils.ANSI_RESET);

        do {
            out.printf("What is the name of the file containing your data?\n");
            filename = inp.next();
            infile = new File(Paths.get("").toAbsolutePath().toString() + "/" + filename);
            if ( !infile.exists() || infile.isDirectory() ) {
                out.printf(MyUtils.ANSI_RED+"[error] The supplied file is either non-existent, read-protected or is a directory."+MyUtils.ANSI_RESET+"\n");
                infile = null;
            }
        } while ( infile == null );
        out.printf(MyUtils.ANSI_GREEN+"[done]"+MyUtils.ANSI_RESET+" reading from %s\n\n",infile.toString());

        bw = new BufferedWriter(new PrintWriter(outfile = new File("./Result.txt")));
        System.setIn(new FileInputStream(new File(filename)));
        go(percent);
    }

    static void go( final double percent ) throws Exception {
        int t = -1;
        boolean flag;
        dataHolder = DataHolder.getInstance(br = new BufferedReader(new InputStreamReader(System.in)));
        Map<Integer, String> attributes = dataHolder.getAllAttributes();
        do {
            out.printf("Please choose an attribute (by number):\n");
            for (Map.Entry<Integer, String> entry : attributes.entrySet())
                out.printf("%8d: %s\n", entry.getKey(), entry.getValue());
            out.printf("\nAttribute: ");
            flag = false ;
            if (!inp.hasNextInt() || (flag = true) && (t = inp.nextInt()) <= 0 || t > attributes.size()) {
                if ( !flag ) inp.next();
                out.printf(MyUtils.ANSI_RED + "[error] please input a number in range 1-" + attributes.size() + MyUtils.ANSI_RESET + "\n");
                t = -1;
            }
        } while (t == -1);
        if ( dataHolder.setTargetAttribute(attributes.get(t)) ) {
            out.printf("\n" + MyUtils.ANSI_GREEN + "[done]" + MyUtils.ANSI_RESET + " target attribute set to "+MyUtils.ASCII_BOLD+attributes.get(t)+MyUtils.ANSI_RESET+"\n\n");
            prepare(percent);
            trainTheClassifier();
            classify();
            out.printf("The result is in the file "+MyUtils.ANSI_CYAN_BACKGROUND+MyUtils.ANSI_RED+outfile.toPath().normalize().toAbsolutePath().toString()+MyUtils.ANSI_RESET+"\n");
            out.println(MyUtils.ANSI_GREEN+"*** Algorithm Finished ***"+ MyUtils.ANSI_RESET);
        } else {
            throw new RuntimeException("DataHolder could not set target variable");
        }
    }

    private static Map<Long,Integer> testingSet, trainingSet;
    private static int TEST_SET_SIZE;

    /*
     * prepares, i.e. splits the data into training and testing datasets
     * according to the supplied percentage
     */
    private static void prepare( double percent ) {
        trainingSet = new HashMap<>();
        testingSet = new HashMap<>();
        Map<Long,Integer> all = dataHolder.retrieveAll(), ptr = trainingSet;
        List<Long> lall = new LinkedList<>();
        for ( Map.Entry<Long,Integer> entry: all.entrySet() )
            for ( int k = 0; k < entry.getValue(); ++k )
                lall.add(entry.getKey());
        Collections.shuffle(lall);
        int n = (int)(percent*lall.size()+1e-9), k = 0;
        TEST_SET_SIZE = dataHolder.getDBSize()-n;
        for ( int i = 0; i < lall.size(); ++i ) {
            Long t = lall.get(i);
            if ( ptr.containsKey(t) )
                ptr.put(t,ptr.get(t)+1);
            else ptr.put(t,1);
            if ( k < n && ++k == n )
                ptr = testingSet;
        }
    }

    static void trainTheClassifier() {
        /*
        int n = (int)(dataHolder.numUniqTuples()*percent);
        Long []t = dataHolder.getUniqTuples(n);
        Map<Long,Integer> cnts = new HashMap<>();
        for ( Long x: t )
            if ( cnts.containsKey(x) )
                cnts.put(x,cnts.get(x)+1);
            else cnts.put(x,1);
            */
        (c = new NaiveBayesClassifier()).trainOnData(trainingSet);
    }

    // used for testing the classifier -- counting accuracy -- on the
    // testing data
    static void classify() {
        int m = dataHolder.getNumOfOutcomes(), same = 0;
        int [][]v = new int[m][m];
        for ( Map.Entry<Long,Integer> entry: testingSet.entrySet() ) {
            Long T = entry.getKey();
            int inReality = dataHolder.getOutcome(T), butClassifiedAs = c.getPrediction(T);
            assert inReality >= 0 && butClassifiedAs >= 0: inReality+" "+butClassifiedAs;
            v[inReality][butClassifiedAs] += entry.getValue();
            same += (inReality==butClassifiedAs?1:0)*entry.getValue();
        }
        try {
            for (int i = 0; i < dataHolder.getN(); ++i)
                bw.write(dataHolder.getNameOfAttribute(i) + " ");
            bw.write("Classification\n");
            for (Map.Entry<Long, Integer> entry : testingSet.entrySet()) {
                Long t = entry.getKey();
                for ( int k = 0; k < entry.getValue(); ++k ) {
                    for (int i = 0; i < dataHolder.getN(); ++i)
                        bw.write(dataHolder.getFieldValueName(t, i) + " ");
                    bw.write(dataHolder.getOutcomeName(c.getPrediction(t)) + "\n");
                }
            }
            StringBuilder sb = new StringBuilder();
            for ( int i = 0; i < dataHolder.getNumOfOutcomes(); ++i )
                sb.append(String.format("%10s",dataHolder.getOutcomeName(i)));
            sb.append("| <--classified as\n");
            for ( int i = 0; i < dataHolder.getNumOfOutcomes(); ++i, sb.append("\n") ) {
                for (int j = 0; j < dataHolder.getNumOfOutcomes(); sb.append(String.format("%10d", v[i][j++]))) ;
                sb.append("| "+dataHolder.getOutcomeName(i));
            }
            bw.write("\n                    === Confusion matrix ===\n\n"+sb.toString());
            bw.write(String.format("\n%% of data used in training = %.2f, Accuracy %d/%d = %.2f\n", percent * 100, same, TEST_SET_SIZE, (same+0.0)/TEST_SET_SIZE));
            bw.flush();
        } catch ( IOException e ) {
            out.println(e.getMessage());
        }
    }
}

