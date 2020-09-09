package network;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.DataSetPreProcessor;
import org.nd4j.linalg.indexing.NDArrayIndex;

class LastStepPreProc implements DataSetPreProcessor {


    @Override
    public void preProcess(DataSet dataSet) {
        INDArray labels = dataSet.getLabels();
        INDArray labelsMaskArray = dataSet.getLabelsMaskArray();

        INDArray labels2d = pullLastTimeSteps(labels, labelsMaskArray);

        dataSet.setLabels(labels2d);
        dataSet.setLabelsMaskArray(null);
    }

    private INDArray pullLastTimeSteps(INDArray labels, INDArray labelsMaskArray) {
        long lastTS = labels.size(2) - 1;
        return labels.get(NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(lastTS));
    }
}