package com

import groovy.transform.CompileStatic
import org.jscience.mathematics.vector.Float64Matrix
import org.jscience.mathematics.vector.Float64Vector

import static com.Utils.collectUpto
import static com.Utils.upto

/**
 *  Created by Artem on 23.04.2017.
 */
class HybridTraining {

    List<List<Double>> x
    List<Double> d

    List<List<Double>> bestC
    List<List<Double>> bestSigma

    List<List<Double>> bestP

    List<List<Double>> bestYAndD

    TskFuzzyNeuralNetwork nn

    int rulesCount
    final double LEARNING_SPEED = 0.0005
    final double TRAINING_SET_PERCENTAGE = 0.7
    final double MAX_ALLOWED_AVERAGE_ERROR = 0.005
    static final int R = 3

    Closure newBestParametersFoundCallback

    HybridTraining(List<List<Double>> x, List<Double> d, int rulesCount, Closure newBestParametersFoundCallback) {
        this.x = x
        this.d = d
        this.newBestParametersFoundCallback = newBestParametersFoundCallback

        this.rulesCount = rulesCount
    }

    void printConfigurationInfo() {
        println "===Info=="
        println "X [training count: ${xn}, validation count: ${allXN - xn}, dimension: $n]"
        println "Training."
        println "====.===="
        println ""
    }

    void train() {
        nn = new TskFuzzyNeuralNetwork(rulesCount)

        printConfigurationInfo()

        boolean needRestartBecauseNaN = doTrain()
        while (needRestartBecauseNaN) {
            println "Restarting because errors = NaN..."
            println ""
            needRestartBecauseNaN = doTrain()
        }
        println "Successfully finished training."
    }

    @CompileStatic
    boolean doTrain() {
        initCmeansCAndSigma()

        double minTotalTrainingAverageError = Integer.MAX_VALUE
        double minTotalValidationAverageError = Integer.MAX_VALUE
        int epoch = 0
        int learningT = 0
        while (minTotalValidationAverageError > MAX_ALLOWED_AVERAGE_ERROR) {
            doTrainingIteration(learningT)

            List<Double> trainingErrors = collectUpto(xn) { int t -> Math.abs(nn.y(x[t]) - d[t]) }
            double totalTrainingAverageError = ((double) trainingErrors.sum()) / xn
            minTotalTrainingAverageError = totalTrainingAverageError < minTotalTrainingAverageError ?
                    totalTrainingAverageError :
                    minTotalTrainingAverageError

            List<Double> validationErrors = collectUpto(xn, allXN) { int t -> Math.abs(nn.y(x[t]) - d[t]) }
            double totalValidationAverageError = ((double) validationErrors.sum()) / (allXN - xn)
            if (totalValidationAverageError < minTotalValidationAverageError) {
                minTotalValidationAverageError = totalValidationAverageError
                bestC = nn.c.collect()
                bestSigma = nn.sigma.collect()
                bestP = nn.p.collect()
                bestYAndD = collectUpto(allXN) { int t -> [nn.y(x[t]), d[t]] }

                def xBackup = x.collect()

                def nextX = x.last().drop(1)
                nextX.add(d.last())
                x.add(nextX)
                def computed = (1..400).collect {
                    def newY = nn.y(x.last())

                    nextX = x.last().drop(1)
                    nextX.add(newY)
                    x.add(nextX)

                    [newY, 0]
                }
                x = xBackup

                newBestParametersFoundCallback.call(bestYAndD, computed)
            }
            minTotalValidationAverageError = totalValidationAverageError < minTotalValidationAverageError ?
                    totalValidationAverageError :
                    minTotalValidationAverageError

            if (Double.isNaN(totalTrainingAverageError) || Double.isNaN(totalValidationAverageError)) {
                return true
            }

            println "Epoch[Iteration]: ${epoch}[$learningT]. ".padRight(26) +
                    "[Training] Total Avg Error: $totalTrainingAverageError. ".padRight(50) +
                    "[Training] Min Total Avg Error: $minTotalTrainingAverageError. ".padRight(50) +
                    "[Validation] Total Avg Error: $totalValidationAverageError. ".padRight(50) +
                    "[Validation] Min Total Avg Error: $minTotalValidationAverageError. ".padRight(50) //+

            learningT++
            if (learningT == xn) {
                epoch++
                learningT = 0
            }
        }

        return false
    }

    @CompileStatic
    private void doTrainingIteration(int learningT) {
        recalculateP()
        recalculateCAndSigma(learningT)
    }

    private void initCmeansCAndSigma() {
        def cMeans = new CMeans(x, m)
        cMeans.cluster()

        nn.c = cMeans.c

        nn.sigma = collectUpto(m) { i ->
            collectUpto(n) { j ->
                def ci = Float64Vector.valueOf(nn.c[i] as double[])

                List<Double> pows = collectUpto(nn.c.size()) { k ->
                    def ck = Float64Vector.valueOf(nn.c[k] as double[])
                    return Math.pow(ci.minus(ck).normValue(), 2)
                }

                return Math.sqrt((double) pows.sort().take(R).sum() / R)
            }
        }
    }

    @CompileStatic
    void recalculateCAndSigma(int t) {
        List<List<Double>> newC = []
        List<List<Double>> newSigma = []
        upto(m) { int i ->
            newC << collectUpto(n) { int j -> nn.c[i][j] - LEARNING_SPEED * de_dc(i, j, t) }
            newSigma << collectUpto(n) { int j -> nn.sigma[i][j] - LEARNING_SPEED * de_dsigma(i, j, t) }
        }
        nn.c = newC
        nn.sigma = newSigma
    }

    @CompileStatic
    double de_dc(int i, int j, int t) {
        double o1 = deCommon(i, t)
        double o2 = de_dc_rightPart(x[t][j], nn.c[i][j], nn.sigma[i][j])
        return o1 * o2
    }

    @CompileStatic
    double de_dsigma(int i, int j, int t) {
        double o1 = deCommon(i, t)
        double o2 = de_dsigma_rightPart(x[t][j], nn.c[i][j], nn.sigma[i][j])
        return o1 * o2
    }

    @CompileStatic
    double deCommon(int i, int t) {
        return (nn.y(x[t]) - d[t]) * (nn.y(i, x[t]) - nn.y(x[t])) * nn.w(i, x[t])
    }

    @CompileStatic
    double de_dc_rightPart(double x, double c, double sigma) {
        return (2 * (x - c)) / Math.pow(sigma, 2)
    }

    @CompileStatic
    double de_dsigma_rightPart(double x, double c, double sigma) {
        return (2 * Math.pow(x - c, 2)) / Math.pow(sigma, 3)
    }

    @CompileStatic
    void recalculateP() {
        double[][] a = collectUpto(xn) { int t ->
            List<Double> row = []

            upto(m) { int i ->
                double w = nn.w(i, x[t])

                row << w
                upto(n) { int j -> row << w * x[t][j] }
            }

            return row
        }

        Float64Matrix aPlus = (Float64Matrix) Float64Matrix.valueOf(a).pseudoInverse()
        Float64Vector dVector = Float64Vector.valueOf(trainingD as double[])
        Float64Vector pVector = aPlus.times(dVector)

        List<Double> allNewP = collectUpto(pVector.dimension) { pVector.get(it).toDouble() }
        List<List<Double>> newP = allNewP.collate(n + 1)
        nn.p = newP
    }

    @CompileStatic
    int getXn() {
        return x.size() * TRAINING_SET_PERCENTAGE
    }

    @CompileStatic
    int getAllXN() {
        return x.size()
    }

    @CompileStatic
    List<Double> getTrainingD() {
        return d.take(xn)
    }

    @CompileStatic
    int getN() {
        return x[0].size()
    }

    @CompileStatic
    int getM() {
        return rulesCount
    }
}
