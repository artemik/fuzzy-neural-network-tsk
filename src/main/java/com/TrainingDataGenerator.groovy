package com
/**
 * Created by Artem on 07.05.2017.
 */
class TrainingDataGenerator {

    static final int WINDOW_SIZE = 4
    static final String TRAINING_SET_DEFAULT_FILE_NAME = "training_set"

    Map generate() {
        return getFromDefaultTrainingSetFile()
    }

    private Map getFromDefaultTrainingSetFile() {
        List<Double> sin = new File(TrainingDataGenerator.classLoader.getResource(TRAINING_SET_DEFAULT_FILE_NAME).getFile()).text
                .split(",")
                .collect { Double.valueOf(it) }

        def (List<List<Double>> x, List<Double> d) = sin.collate(WINDOW_SIZE, 1, false).inject([[], []]) { xAndD, sinSubList ->
            xAndD[0] << sinSubList.dropRight(1)
            xAndD[1] << sinSubList.last()
            xAndD
        }

        return [
                "x": x,
                "d": d
        ]
    }
}
