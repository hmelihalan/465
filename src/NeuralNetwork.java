public class NeuralNetwork {

    int inputSize = 8;
    int hiddenSize = 12;
    int outputSize = 4; // UP, DOWN, LEFT, RIGHT

    double[][] w1 = new double[inputSize][hiddenSize];
    double[] b1 = new double[hiddenSize];

    double[][] w2 = new double[hiddenSize][outputSize];
    double[] b2 = new double[outputSize];

    public NeuralNetwork() {
        init(w1);
        init(w2);
    }

    private void init(double[][] w) {
        for (int i = 0; i < w.length; i++)
            for (int j = 0; j < w[0].length; j++)
                w[i][j] = Math.random() * 2 - 1;
    }

    public int predict(double[] input) {

        double[] hidden = new double[hiddenSize];

        // INPUT → HIDDEN
        for (int j = 0; j < hiddenSize; j++) {
            double sum = 0;
            for (int i = 0; i < inputSize; i++) {
                sum += input[i] * w1[i][j];
            }
            hidden[j] = Math.max(0, sum + b1[j]); // ReLU
        }

        double[] out = new double[outputSize];

        // HIDDEN → OUTPUT
        for (int k = 0; k < outputSize; k++) {
            double sum = 0;
            for (int j = 0; j < hiddenSize; j++) {
                sum += hidden[j] * w2[j][k];
            }
            out[k] = sum + b2[k];
        }

        return argMax(out);
    }

    private int argMax(double[] x) {
        int idx = 0;
        for (int i = 1; i < x.length; i++)
            if (x[i] > x[idx]) idx = i;
        return idx;
    }
}
