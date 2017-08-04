package com

import org.jfree.ui.RefineryUtilities

class Launcher {
    static final int RULES_COUNT = 6

    static void main(String[] args) {
        Map trainData = new TrainingDataGenerator().generate()
        List<List<Double>> xx = (List<List<Double>>) trainData["x"]
        List<Double> dd = (List<Double>) trainData["d"]

        GraphFrame graph = createGraphAndShow()

        HybridTraining main = new HybridTraining(xx, dd, RULES_COUNT, { List yAndDList, List fAndDList ->
            graph.refresh(yAndDList, fAndDList)
        })
        main.train()
    }

    private static GraphFrame createGraphAndShow() {
        GraphFrame graph = new GraphFrame("Best Computed and Original");
        graph.pack();
        RefineryUtilities.centerFrameOnScreen(graph);
        graph.setVisible(true);
        return graph;
    }
}
