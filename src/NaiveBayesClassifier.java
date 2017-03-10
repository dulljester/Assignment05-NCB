import java.util.*;

public class NaiveBayesClassifier implements Classifier {
    private int[] card = new int[2];
    private DataHolder dataHolder = DataHolder.getInstance(null);
    Map<Long,Integer> trainingData;
    private Summarizer summarizer = null;

    private class Summarizer {
        private Map<Integer,Map<Integer,Map<Long,Integer>>> marginals = new HashMap<>();
        Summarizer() {
            for ( int i = 0; i <= 1; ++i ) {
                marginals.put(i, new HashMap<>());
                for ( int j = 0; j < dataHolder.getN()-1; ++j )
                    marginals.get(i).put(j,new HashMap<>());
            }
            for ( int i = 0; i <= 1; card[i++] = 0 );
            for ( Map.Entry<Long,Integer> entry: trainingData.entrySet() ) {
                Long t = entry.getKey();
                int classLabel = dataHolder.getOutcome(t);
                card[classLabel] += entry.getValue();
                Map<Integer,Map<Long,Integer>> mp = marginals.get(classLabel);
                for ( int i = 0; i < dataHolder.getN()-1; ++i ) {
                    Long val = dataHolder.readAttribute(t,i);
                    if ( !mp.get(i).containsKey(val) )
                        mp.get(i).put(val,0);
                    mp.get(i).put(val,mp.get(i).get(val)+entry.getValue());
                }
            }
        }
        /**
         * @param idx index of the attribute (aka field, column)
         * @param value the value of the attribute
         * @return logarithm of the conditional probability that a field "idx" assumes the value "value" given the class label (uses Laplace smoothing)
         */
        double getLogConditionalProbability( int idx, long value, int classLabel ) {
            Map<Long,Integer> mp = marginals.get(classLabel).get(idx);
            /* Laplace Smoothing: */
            int up = (1+(!mp.containsKey(value)?0:mp.get(value))), down = (dataHolder.getDomainCardinality(idx)+card[classLabel]);
            return Math.log(up)-Math.log(down);
        }
    }

    @Override
    /**
     * @param trainingData: (tuple,numOfSuchTuplesInDatabase)
     */
    public void trainOnData( Map<Long,Integer> trainingData ) {
        this.trainingData = trainingData;
        summarizer = new Summarizer();
    }
    @Override
    /**
     * @param t tuple
     * @return 0/1 as a predicted class label
     */
    public int getPrediction(Long t) {
        double []mass = new double[2];
        for ( int c = 0; c <= 1; ++c )
            for ( int i = 0; i < dataHolder.getN()-1; ++i )
                mass[c] += summarizer.getLogConditionalProbability(i,dataHolder.readAttribute(t,i),c);
        if ( Math.abs(mass[0]-mass[1]) < MyUtils.tol )
            return MyUtils.randint(0,1);
        return mass[0]<mass[1]?1:0;
    }
}

