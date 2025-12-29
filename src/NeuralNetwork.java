public class NeuralNetwork {

    int inputSize = 4;
    int hiddenSize = 4;
    int outputSize = 1; // SCORE

    double[][] w1 = new double[inputSize][hiddenSize];
    double[] b1 = new double[hiddenSize];

    double[] w2 = new double[hiddenSize];
    double b2 = 0;

    public NeuralNetwork() {
        init();
    }

    private void init() {

        // H0: chaser distance (EN ÖNEMLİ)
        w1[0][0] = 6.0;

        // H1: second chaser distance
        w1[1][1] = 3.0;

        // H2: wall penalty
        w1[2][2] = -1.2;

        // H3: dead-end penalty
        w1[3][3] = -1.0;

        // OUTPUT
        w2[0] = 4.5;   // yakın chaser'dan uzaklaşmak = büyük ödül
        w2[1] = 2.5;
        w2[2] = 1.0;
        w2[3] = 1.0;

        b2 = 0.0;
    }

    public double score(double[] in) {

        double[] h = new double[hiddenSize];

        for (int j = 0; j < hiddenSize; j++) {
            double sum = 0;
            for (int i = 0; i < inputSize; i++)
                sum += in[i] * w1[i][j];
            h[j] = Math.max(0, sum + b1[j]);
        }

        double out = 0;
        for (int j = 0; j < hiddenSize; j++)
            out += h[j] * w2[j];

        return out + b2;
    }
}
