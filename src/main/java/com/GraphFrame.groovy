package com

import org.jfree.chart.ChartFactory

/**
 * Created by Artem on 04.05.2017.
 */
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.plot.XYPlot
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.XYDataset
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.ui.ApplicationFrame

import java.awt.*
import java.util.List;

class GraphFrame extends ApplicationFrame {

    XYDataset dataset

    GraphFrame(final String title) {

        super(title);
        setPreferredSize(new Dimension(600, 400))
    }

    private void refreshDataset(List yAndDList, List fAndDList) {
        XYSeries computed = new XYSeries("Computed")
        XYSeries original = new XYSeries("Original")
        yAndDList.eachWithIndex { List yAndD, index ->
            computed.add(index, (double) yAndD[0])
            original.add(index, (double) yAndD[1])
        }
        XYSeries forecast = new XYSeries("Forecast")
        fAndDList.eachWithIndex { List fAndD, index ->
            forecast.add(yAndDList.size() + index, (double) fAndD[0])
        }

        dataset = new XYSeriesCollection()
        dataset.addSeries(computed)
        dataset.addSeries(original)
        dataset.addSeries(forecast)

        JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "X",
                "Y",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                false,
                false
        );
        XYPlot plot = (XYPlot) chart.getPlot();

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, true);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesLinesVisible(2, true);
        renderer.setSeriesShapesVisible(2, false);
        plot.setRenderer(renderer);

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 300));
        setContentPane(chartPanel);
    }

    void refresh(List yAndDList, List fAndDList) {
        refreshDataset(yAndDList, fAndDList)
        repaint()
        revalidate()
    }
}