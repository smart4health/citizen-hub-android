package pt.uninova.s4h.citizenhub.ui.summary;

public class TwoDimensionalChartData {

    private final double[][] data;
    private final int x;
    private final int y;

    public TwoDimensionalChartData(int x, int y){
        data = new double[x][y];
        this.x = x;
        this.y = y;
    }

    public double get(int x, int y){return data[x][y];}

    public int getX(){return x;}

    public int getY(){return y;}

    public void set(int x, int y, double value){data[x][y] = value;}

}
