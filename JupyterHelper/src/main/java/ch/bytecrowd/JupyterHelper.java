package ch.bytecrowd;

import com.opencsv.CSVReader;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.knowm.xchart.BitmapEncoder.BitmapFormat.PNG;
import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.*;

public class JupyterHelper {

    /**
     * @param path: String
     * @param skipFirstLine: boolean
     * @return LinkedHashMap<Integer, List<String>>
     *
     * returns the CSV columns in a Map, the Key is the column index
     */
    public static LinkedHashMap<Integer, List<String>> readCsvColumns(String path, boolean skipFirstLine) {
        try (final var reader = Files.newBufferedReader(Paths.get(path));
             final var csvReader = new CSVReader(reader);){

            final var columns = new LinkedHashMap<Integer, List<String>>();
            final var csvLines = csvReader.readAll();
            for (var row = skipFirstLine ? 1 : 0; row < csvLines.size(); row++) {
                for (var column = 0; column < csvLines.get(row).length; column++) {
                    if (!columns.containsKey(column)) {
                        columns.put(column, new ArrayList<>());
                    }
                    columns.get(column).add(csvLines.get(row)[column]);
                }
            }
            return columns;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param command: String
     */
    public static void executeOsCommand(String command) {
        final var isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        final var cmd = new String[]{
                isWindows ? "cmd" : "sh",
                isWindows ? "/c" : "-c",
                command
        };
        executeOsCommand(cmd);
    }

    private static void executeOsCommand(String[] cmd) {
        System.out.println("Executing OS command: " + Arrays.stream(cmd).collect(Collectors.joining(" ")) + "\n");
        try {
            final var process = Runtime.getRuntime().exec(cmd);
            handleOsProcess(process);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleOsProcess(Process process) throws IOException {
        try (final var inputStream = process.getInputStream();
             final var errorStream = process.getErrorStream();){

            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length > 0) {
                System.out.println(new String(bytes));
            }
            byte[] errorBytes = errorStream.readAllBytes();
            if (errorBytes.length > 0) {
                System.err.println(new String(errorBytes));
            }
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * @param url: String
     * @param path: String
     * @throws RuntimeException
     */
    public static void downloadFile(String url, String path) {
        try (final var stream = new URL(url).openStream()){

            Files.copy(stream, Paths.get(path), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param path: String
     * @return java.awt.image.BufferedImage
     * @throws IOException
     */
    public static BufferedImage showImage(String path) throws IOException {
        return ImageIO.read(new File(path));
    }

    /**
     * @param label: String
     * @param xValues: List&lt;Number&gt;
     * @param yValues: List&lt;Number&gt;
     */
    public record XYSeries(String label, List<Number> xValues, List<Number> yValues) {
        public void addToChart(XYChart chart) {
            chart.addSeries(label, xValues, yValues);
        }
        public XYSeries withNewLabel(String newLabel) {
            return new XYSeries(newLabel, xValues, yValues);
        }
    }

    /**
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @return java.awt.image.BufferedImage
     */
    public static BufferedImage plotScatterChart(List<XYSeries> series) {
        return plotScatterChart("X", "Y", series);
    }

    /**
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @return java.awt.image.BufferedImage
     */
    public static BufferedImage plotLineChart(List<XYSeries> series) {
        return plotLineChart("X", "Y", series);
    }

    /**
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @return java.awt.image.BufferedImage
     */
    public static BufferedImage plotAreaChart(List<XYSeries> series) {
        return plotAreaChart("X", "Y", series);
    }

    /**
     * @param xAxisTitle: String
     * @param yAxisTitle: String
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @return java.awt.image.BufferedImage
     */
    public static BufferedImage plotScatterChart(String xAxisTitle, String yAxisTitle, List<XYSeries> series) {
        return plotXYChart(xAxisTitle, yAxisTitle, series, Scatter);
    }

    /**
     * @param xAxisTitle: String
     * @param yAxisTitle: String
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @return java.awt.image.BufferedImage
     */
    public static BufferedImage plotLineChart(String xAxisTitle, String yAxisTitle, List<XYSeries> series) {
        return plotXYChart(xAxisTitle, yAxisTitle, series, Line);
    }

    /**
     * @param xAxisTitle: String
     * @param yAxisTitle: String
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @return java.awt.image.BufferedImage
     */
    public static BufferedImage plotAreaChart(String xAxisTitle, String yAxisTitle, List<XYSeries> series) {
        return plotXYChart(xAxisTitle, yAxisTitle, series, Area);
    }

    /**
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @return java.awt.image.BufferedImage
     */
    public static BufferedImage plotCrossChart(List<XYSeries> series) {
        var charts = new ArrayList<Chart>();
        int seriesSize = series.size();
        for (var i = 0; i < seriesSize; i++) {
            for (var j = 0; j < seriesSize; j++) {
                var xySeries_i = series.get(i);
                var xySeries_j = series.get(j);
                var chart = createXyChart(
                        "X", "Y",
                        List.of(
                                xySeries_i.withNewLabel(xySeries_i.label + " i:" + i),
                                xySeries_j.withNewLabel(xySeries_j.label + " j:" + j)
                        ),
                        Scatter
                );
                charts.add(chart);
            }
        }
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("chart_", ".png");
            BitmapEncoder.saveBitmap(charts, seriesSize, seriesSize, tempFile.toAbsolutePath().toString(), PNG);
            return ImageIO.read(tempFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Files.delete(tempFile);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * @param xAxisTitle: String
     * @param yAxisTitle: String
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @param renderStyle: org.knowm.xchart.XYSeries.XYSeriesRenderStyle
     * @return java.awt.image.BufferedImage
     */
    private static BufferedImage plotXYChart(
            String xAxisTitle,
            String yAxisTitle,
            List<XYSeries> series,
            org.knowm.xchart.XYSeries.XYSeriesRenderStyle renderStyle) {
        final XYChart chart = createXyChart(xAxisTitle, yAxisTitle, series, renderStyle);
        return BitmapEncoder.getBufferedImage(chart);
    }

    /**
     * @param xAxisTitle: String
     * @param yAxisTitle: String
     * @param series: java.util.List&lt;ch.bytecrowd.JupyterHelper.XYSeries&gt;
     * @param renderStyle: org.knowm.xchart.XYSeries.XYSeriesRenderStyle
     * @return java.awt.image.BufferedImage
     */
    private static XYChart createXyChart(
            String xAxisTitle,
            String yAxisTitle,
            List<XYSeries> series,
            org.knowm.xchart.XYSeries.XYSeriesRenderStyle renderStyle) {
        final var chart = new XYChartBuilder()
                .xAxisTitle(xAxisTitle)
                .yAxisTitle(yAxisTitle)
                .build();
        chart.getStyler().setDefaultSeriesRenderStyle(renderStyle);
        series.stream()
                .forEach(xySeries -> xySeries.addToChart(chart));
        return chart;
    }
}