package com

import org.jscience.mathematics.vector.Float64Vector

import static com.Utils.collectUpto
import static com.Utils.sum

/**
 * Created by Artem on 08.05.2017.
 */
class CMeans {
    List<List<Double>> x
    int m

    List<List<Double>> u
    List<List<Double>> c

    static final double DIFFERENCE_THRESHOLD = 0.0000001

    CMeans(List<List<Double>> x, int m) {
        this.x = x
        this.m = m
    }

    void cluster() {
        initRandomU()

        double previousE = Double.MAX_VALUE
        while (true) {
            calculateC()

            double e = e()

            calculateU()

            double difference = previousE - e
            if (difference < DIFFERENCE_THRESHOLD) {
                break
            }
            println "$e : $difference"
            previousE = e
        }
    }

    void calculateC() {
        c = collectUpto(m) { i ->
            def zeroVector = Float64Vector.valueOf(new double[n])
            Float64Vector nominator = (0..<tp).inject(zeroVector) { sum, t ->
                def xt = Float64Vector.valueOf(x[(int) t] as double[])
                def current = xt.times(Math.pow(u[(int) t][i], 2))
                return ((Float64Vector) sum).plus(current)
            }

            double denominator = sum(tp) { t -> Math.pow(u[(int) t][i], 2) }

            def ci = nominator.times(1 / denominator)
            return collectUpto(ci.dimension) { ci.get(it).toDouble() }
        }
    }

    void calculateU() {
        u = collectUpto(tp) { t ->
            collectUpto(m) { i ->
                double dItSquared = Math.pow(d(i, t), 2)
                double sum = sum(m) { k ->
                    double dKtSquared = Math.pow(d(k, t), 2)
                    double q = m == 1 ? 1 : m - 1
                    return Math.pow(dItSquared / dKtSquared, 1 / (q))
                }
                return 1 / sum
            }
        }
    }

    double d(int i, int t) {
        def cVector = Float64Vector.valueOf(c[i] as double[])
        def xVector = Float64Vector.valueOf(x[t] as double[])
        return cVector.minus(xVector).normValue()
    }

    double e() {
        sum(m) { i ->
            def cVector = Float64Vector.valueOf(c[i] as double[])
            sum(tp) { t ->
                def xVector = Float64Vector.valueOf(x[t] as double[])
                def cMinusX = cVector.minus(xVector)
                return Math.pow(u[(int) t][i], 2) * Math.pow(cMinusX.normValue(), 2)
            }
        }
    }

    void initRandomU() {
        u = collectUpto(tp) { getRandomU() }
    }

    List<Double> getRandomU() {
        List<Double> u = collectUpto(m) { Math.random() }
        double sum = (double) u.sum()
        u = u.collect { it / sum }
        return u
    }

    int getTp() {
        return x.size()
    }

    int getN() {
        return x[0].size()
    }
}
