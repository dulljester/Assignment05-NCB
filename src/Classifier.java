import java.util.Map;

/**
 * Created by sj on 16/02/17.
 */
public interface Classifier {
    void trainOnData( final Map<Long,Integer> trainingData ) ;
    int getPrediction( Long t ) ;
}
