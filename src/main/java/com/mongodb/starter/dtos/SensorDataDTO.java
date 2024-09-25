package com.mongodb.starter.dtos;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A DTO for the {@link com.mongodb.starter.models.SensorData} entity.
 */
public class SensorDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    @NotNull
    private Float temperature;

    @NotNull
    private Float humidity;

    @NotNull
    private Float light;

    @NotNull
    private String time;

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public Float getHumidity() {
        return humidity;
    }

    public void setHumidity(Float humidity) {
        this.humidity = humidity;
    }

    public Float getLight() {
        return light;
    }

    public void setLight(Float light) {
        this.light = light;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    // equals, hashCode, toString

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SensorDataDTO)) {
            return false;
        }
        return id != null && id.equals(((SensorDataDTO) o).id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "SensorDataDTO{" +
                "id='" + getId() + '\'' +
                ", temperature=" + getTemperature() +
                ", humidity=" + getHumidity() +
                ", light=" + getLight() +
                ", time='" + getTime() + '\'' +
                '}';
    }
}
