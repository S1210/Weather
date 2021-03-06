
package ru.alex.weather.models.current;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Imperial {

    @SerializedName("Value")
    @Expose
    private int value;
    @SerializedName("Unit")
    @Expose
    private String unit;
    @SerializedName("UnitType")
    @Expose
    private int unitType;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getUnitType() {
        return unitType;
    }

    public void setUnitType(int unitType) {
        this.unitType = unitType;
    }

}
