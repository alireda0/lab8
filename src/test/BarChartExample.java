package test;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.JFrame;

public class BarChartExample extends JFrame {

    public BarChartExample(String title) {
        super(title);

        // 1. Create Dataset
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(10, "Marks", "Ahmed");
        dataset.addValue(15, "Marks", "Omar");
        dataset.addValue(20, "Marks", "Yassin");
        dataset.addValue(18, "Marks", "Mariam");

        // 2. Create Chart
        JFreeChart barChart = ChartFactory.createBarChart(
                "Student Marks",      // Chart Title
                "Student",            // Category Axis Label
                "Marks",              // Value Axis Label
                dataset
        );

        // 3. Add chart to panel
        ChartPanel panel = new ChartPanel(barChart);
        setContentPane(panel);
    }

    public static void main(String[] args) {
        BarChartExample example = new BarChartExample("Bar Chart Example");
        example.setSize(800, 600);
        example.setLocationRelativeTo(null);
        example.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        example.setVisible(true);
    }
}
