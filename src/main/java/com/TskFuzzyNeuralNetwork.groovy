package com

import static com.Utils.product
import static com.Utils.sum

/**
 *  Created by Artem on 23.04.2017.
 */
class TskFuzzyNeuralNetwork {
    List<List<Double>> c
    List<List<Double>> sigma

    List<List<Double>> p

    int rulesCount

    TskFuzzyNeuralNetwork(int rulesCount) {
        this.rulesCount = rulesCount
    }

    double y(List<Double> x) {
        double nominator = sum(m, { i -> w(i, x) * y(i, x) })
        double denominator = sum(m, { i -> w(i, x) })
        return nominator / denominator
    }

    double y(int i, List<Double> x) {
        return p[i][0] + sum(x.size(), { j -> p[i][j] * x[j] })
    }

    double w(int i, List<Double> x) {
        return mu(i, x) / sum(m, { ii -> mu(ii, x) })
    }

    double mu(int i, List<Double> x) {
        return product(x.size(), { j -> mu(i, j, x) })
    }

    double mu(int i, int j, List<Double> x) {
        return mu(x[j], c[i][j], sigma[i][j])
    }

    double mu(double x, double c, double sigma) {
        double o1 = Math.pow((x - c) / sigma, 2)
        return Math.exp(-o1)
    }

    int getM() {
        return rulesCount
    }
}
