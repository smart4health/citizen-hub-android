package pt.uninova.s4h.citizenhub.persistence.entity.util;

public class AbstractBloodPressurePanel implements BloodPressurePanel {

    private Double diastolic;
    private Double mean;
    private Double systolic;

    protected AbstractBloodPressurePanel(Double diastolic, Double mean, Double systolic){
        this.diastolic = diastolic;
        this.mean = mean;
        this.systolic = systolic;
    }

    public Double getDiastolic(){ return diastolic; }

    public Double getMean(){
        return mean;
    }

    public Double getSystolic(){
        return systolic;
    }

    public void setDiastolic(Double value) { this.diastolic = value; }

    public void setMean(Double value) { this.mean = value; }

    public void setSystolic(Double value) { this.systolic = value; }

}
